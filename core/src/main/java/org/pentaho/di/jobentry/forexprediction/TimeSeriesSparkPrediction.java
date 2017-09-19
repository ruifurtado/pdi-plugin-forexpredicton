package org.pentaho.di.jobentry.forexprediction;

import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.regression.RandomForestRegressionModel;
import org.apache.spark.ml.regression.RandomForestRegressor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.pentaho.di.jobentry.forexprediction.functions.OutputFeatures;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import static org.apache.spark.sql.functions.max;
import static org.apache.spark.sql.functions.min;

public class TimeSeriesSparkPrediction {

    private static final int MAXDEPTH = 5;
    private static final int NUMTREES =10;

    private static final String DATE = "Date";
    private String trainPath;
    private String testPath;
    private String outputFilePath;
    private String marketName;
    private RandomForestRegressionModel rfModel;
    private List<Double> volatility=new ArrayList<>();
    private List<Double> priceChange=new ArrayList<>();
    private boolean isToSave;
    private boolean isToLoad;
    private String loadModelFolder;
    private double fullVolatility;
    private double lastTrainValue;
    private double lastPredictedValue;
    private List<String> outputFileline=new ArrayList<>();
    private String configFile;
    private int numTrees;
    private int maxDepth;
    private String evaluation;
    private SparkSession spark;

    public TimeSeriesSparkPrediction(String marketName, String outputFilePath , String loadModelFolder , boolean isToSave, boolean isToLoad, String configFile){
        this.outputFilePath=outputFilePath;
        this.loadModelFolder=loadModelFolder;
        this.marketName=getNameForFile(marketName);
        this.trainPath=outputFilePath+"\\"+this.marketName+"TrainFile.csv";
        this.testPath=outputFilePath+"\\"+this.marketName+"TestFile.csv";
        this.isToSave=isToSave;
        this.isToLoad=isToLoad;
        this.configFile=configFile;
    }

    public void TimeSeriesPredict() throws IOException {
         spark = SparkSession.builder()
                .master("local")
                .appName("TimeSeries")
                .getOrCreate();

        StructType schema = new StructType(new StructField[]{
                new StructField(DATE, DataTypes.DateType, true, Metadata.empty()),
                new StructField("Close", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("High", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("Low", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("SMA", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("EMA", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("WMA", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("MOM", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("MACD", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("RSI",DataTypes.DoubleType,true,Metadata.empty()),
                new StructField("t-1",DataTypes.DoubleType,true,Metadata.empty()),
                new StructField("t-2",DataTypes.DoubleType,true,Metadata.empty()),
                new StructField("t-3",DataTypes.DoubleType,true,Metadata.empty()),
                new StructField("label", DataTypes.DoubleType, true, Metadata.empty()),
        });

        StructType schema2 = new StructType(new StructField[]{
                new StructField(DATE, DataTypes.DateType, true, Metadata.empty()),
                new StructField("Close", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("High", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("Low", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("SMA", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("EMA", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("WMA", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("MOM", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("MACD", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("RSI",DataTypes.DoubleType,true,Metadata.empty()),
                new StructField("t-1",DataTypes.DoubleType,true,Metadata.empty()),
                new StructField("t-2",DataTypes.DoubleType,true,Metadata.empty()),
                new StructField("t-3",DataTypes.DoubleType,true,Metadata.empty()),
                new StructField("label", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("prediction", DataTypes.DoubleType, true, Metadata.empty()),
        });

        String[] features = new String[]{"Close", "High", "Low", "SMA","EMA","WMA","MOM","MACD", "RSI","t-1","t-2","t-3"};

        Dataset<Row> trainData = spark.read()
                .schema(schema)
                .option("header", "false")
                .csv(trainPath);

        Dataset<Row> testData = spark.read()
                .schema(schema)
                .option("header", "false")
                .csv(testPath);

        VectorAssembler assembler = new VectorAssembler()
                .setInputCols(features)
                .setOutputCol("features");

        trainData = assembler.transform(trainData);
        testData=assembler.transform(testData);

        initializeModel(trainData);  //IF LOAD ACTIVE, LOADS FILE. IF NOT, TRAINS THE MODEL

        Dataset<Row> predictions = rfModel.transform(testData);

        List<Row> evalList=extractEvaluationSet(predictions);
        Dataset<Row> evaluationSet=spark.createDataFrame(evalList,schema2);
        evaluateMarket(evaluationSet);

        saveModel();

        OutputFeatures v=new OutputFeatures(trainData,predictions);
        fullVolatility=v.getFullVolatility();
        volatility=v.getVol();
        priceChange=v.getPcx();
        lastTrainValue=v.getLastTrainValue();
        lastPredictedValue=v.getLastPredictedValue();

        try {
            writeMarketTextFile(predictions.collectAsList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        createOutputTextFileLine();
        spark.stop();
        cleanTrainTestFiles();

    }

    public void writeMarketTextFile(List<Row> pred) throws IOException {
        File outputFile=new File(outputFilePath,marketName+"_OutputFile.csv");
        if(outputFile.exists()){
            outputFile.delete();
            outputFile.createNewFile();
            String l="DATE"+";"+"PREDICTION"+";"+"DIRECTION"+";"+"PRICE CHANGE %"+";"+"VOLATILITY %"+"\n";
            Files.write(outputFile.toPath(), l.getBytes(), StandardOpenOption.WRITE);
        }
        if(!outputFile.exists()){
            outputFile.createNewFile();
            String l="DATE"+";"+"PREDICTION"+";"+"DIRECTION"+";"+"PRICE CHANGE %"+";"+"VOLATILITY %"+"\n";
            Files.write(outputFile.toPath(), l.getBytes(), StandardOpenOption.WRITE);
        }
        for(int i=0;i<pred.size();i++) {
            Date date=pred.get(i).getDate(pred.get(i).fieldIndex("Date"));
            String dt=calculateDate(date,pred.size()/2);
            String direction=calculateDirection(priceChange.get(i));
            String line=dt+";"+ pred.get(i).getDouble(pred.get(i).fieldIndex("prediction"))+";"+direction+";"+priceChange.get(i)+";"+volatility.get(i)+"\n";
            try {
                Files.write(outputFile.toPath(), line.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void readConfigFile() {
        if (configFile != null) {
            Properties prop = new Properties();
            InputStream input = null;
            try {
                input = new FileInputStream(configFile);

                // load a properties file
                prop.load(input);
                maxDepth = Integer.parseInt(prop.getProperty("maxDepth"));
                numTrees = Integer.parseInt(prop.getProperty("numTrees"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {  //default values
            numTrees = NUMTREES;
            maxDepth =MAXDEPTH;
        }
    }

    private void initializeModel(Dataset<Row> data){
        if(isToLoad){
            rfModel=RandomForestRegressionModel.load(loadModelFolder+"\\"+marketName);
        }
        else{
            readConfigFile();
            RandomForestRegressor rf = new RandomForestRegressor()
                    .setNumTrees(numTrees)
                    .setMaxDepth(maxDepth);
            rfModel = rf.fit(data);
        }
    }


    private void saveModel(){
        if(isToSave) {
            try {
                rfModel.write().overwrite().save(outputFilePath + "\\" + marketName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String calculateDate(Date baseDate, int numberOfDays){

        if(baseDate == null){
            baseDate = new Date();
        }

        Calendar baseDateCal = Calendar.getInstance();
        baseDateCal.setTime(baseDate);

        for(int i = 0; i < numberOfDays; i++){

            baseDateCal.add(Calendar.DATE,1);
            if(baseDateCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
                baseDateCal.add(Calendar.DATE,2);
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date=sdf.format(baseDateCal.getTime());
        return date;
    }

    private String calculateDirection(Double d){
        String answer=null;
        if(d>0){
            answer="UP";
        }
        if(d==0){
            answer="SIDEWAYS";
        }
        if(d<0){
            answer="DOWN";
        }
        return answer;
    }

    private double calculatePriceChange(double lastPredictedValue,double lastTrainValue){
        double answer;
        answer=((lastPredictedValue-lastTrainValue)/lastTrainValue)*100;
        return answer;
    }

    private String getNameForFile(String filepath){
        String f=new File(filepath).getName();
        String [] splitter =f.split(".csv");
        return splitter[0];
    }

    private void cleanTrainTestFiles(){
        File trainFile=new File(trainPath);
        File testFile= new File(testPath);
        trainFile.delete();
        testFile.delete();

    }

    private void createOutputTextFileLine(){
        double aproxPriceChange=calculatePriceChange(lastPredictedValue,lastTrainValue);
        String direction=calculateDirection(aproxPriceChange);
        outputFileline.add(marketName);
        outputFileline.add(direction);
        outputFileline.add(String.valueOf(aproxPriceChange));
        outputFileline.add(String.valueOf(fullVolatility));
    }

    private void evaluateMarket(Dataset<Row> set){
        RegressionEvaluator evaluator = new RegressionEvaluator()
                .setLabelCol("label")
                .setPredictionCol("prediction")
                .setMetricName("rmse");

        double rmse = evaluator.evaluate(set);
        evaluator.setMetricName("mse");
        double mse = evaluator.evaluate(set);
        evaluator.setMetricName("mae");
        double mae =evaluator.evaluate(set);

        Dataset<Row> metrics=set.agg(max(set.col("prediction")),min(set.col("prediction")));
        double max=metrics.collectAsList().get(0).getDouble(metrics.collectAsList().get(0).fieldIndex("max(prediction)"));
        double min=metrics.collectAsList().get(0).getDouble(metrics.collectAsList().get(0).fieldIndex("min(prediction)"));
        evaluation="MARKET NAME: "+marketName+"\n"+"MAX MARKET VALUE: "+max+" MIN MARKET VALUE: "+min+"\n"+"REGRESSION EVALUATION METRICS: "+" RMSE: "+rmse+" MSE: "+mse+" MAE: "+mae+"\n\n";
    }


    public List<String> getOutputFileline() {
        return outputFileline;
    }

    public String getMarketEvaluation(){
        return evaluation;
    }

    private List<Row> extractEvaluationSet(Dataset<Row> p) {
        Dataset<Row> predWithoutFeatures=p.select("Date","Close", "High", "Low", "SMA","EMA","WMA","MOM","MACD", "RSI","t-1","t-2","t-3","label","prediction");
        List<Row> pred=predWithoutFeatures.collectAsList();
        List<Row> half = new ArrayList<>();
        for (int i = 0; i < pred.size(); i++) {
            try {
                double label = pred.get(i).getDouble(pred.get(i).fieldIndex("label"));
                half.add(pred.get(i));
            } catch (Exception e) {
                break;
            }
        }
        return half;
    }
}

