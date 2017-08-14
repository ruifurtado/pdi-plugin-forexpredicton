package org.pentaho.di.jobentry.forexprediction;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.regression.GBTRegressionModel;
import org.apache.spark.ml.regression.GBTRegressor;
import org.apache.spark.ml.regression.RandomForestRegressionModel;
import org.apache.spark.ml.regression.RandomForestRegressor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.pentaho.di.jobentry.forexprediction.functions.Volatilty;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.StrictMath.toIntExact;

public class TimeSeriesSparkPrediction {

    private static final int MAXBINS = 32;
    private static final int ITERATIONS = 20;
    private static final String LOSSTYPE = "squared";
    private static final int MAXDEPTH = 5;
    private static final String IMPURITY = "variance";
    private static final double STEPSIZE = 0.1;
    private static final double SUBSAMPLINGRATE = 1.0;

    private static final String DATE = "Date";
    public String trainPath;
    public String testPath;
    public String outputFilePath;
    public String saveModelName;
    public String toLoadFile;
    public String marketName;
    private GBTRegressor gbt;
    private GBTRegressionModel gbtModel;
    private List<Double> volatility=new ArrayList<>();
    private List<Double> priceChange=new ArrayList<>();
    private int fileNumber;
    private boolean isToSave;
    private boolean isToLoad;
    private String loadModelFolder;
    private int addForecastingDays=0;
    private double fullVolatility;
    private double lastTrainValue;
    private double lastPredictedValue;
    private List<String> outputFileline=new ArrayList<>();
    private String configFile;
    private int maxBins;
    private int iterations;
    private String lossType;
    private int maxDepth;
    private String impurity;
    private double stepSize;
    private double subsamplingRate;

    public TimeSeriesSparkPrediction(String marketName, String outputFilePath , String loadModelFolder , boolean isToSave, boolean isToLoad, int fileNumber, String configFile){
        this.fileNumber=fileNumber;
        this.outputFilePath=outputFilePath;
        this.loadModelFolder=loadModelFolder;
        this.marketName=getNameForFile(marketName);
        this.trainPath=outputFilePath+"\\"+this.marketName+"TrainFile.csv";
        this.testPath=outputFilePath+"\\"+this.marketName+"TestFile.csv";
        this.isToSave=isToSave;
        this.isToLoad=isToLoad;
        this.configFile=configFile;
    }

    public void TimeSeriesPredict() {
        SparkSession spark = SparkSession.builder()
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
                new StructField("RSI", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("label", DataTypes.DoubleType, true, Metadata.empty()),
                //new StructField("Close-2", DataTypes.DoubleType, true, Metadata.empty()),
                //new StructField("Close-1", DataTypes.DoubleType, true, Metadata.empty()),
        });

        String[] features = new String[]{"Close", "High", "Low", "SMA", "EMA","WMA","MOM","MACD","RSI"};
        //String label = "Close";

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

        Dataset<Row> predictions = gbtModel.transform(testData);

        saveModel();

        Volatilty v=new Volatilty(trainData,predictions);
        v.calculateVolatility();
        v.calculatePredVolatility();
        fullVolatility=v.getFullVolatility();
        volatility=v.getVol();
        priceChange=v.getPcx();
        lastTrainValue=v.getLastTrainValue();
        lastPredictedValue=v.getLastPredictedValue();

        try {
            writeMarketTextFile(predictions.collectAsList());
            createOutputTextFileLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        spark.stop();
        cleanTrainTestFiles();

    }

    public void writeMarketTextFile(List<Row> pred) throws IOException {
        File outputFile=new File(outputFilePath,marketName+"OutputFile.csv");
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
            String dt=calculateDate(date,pred.size());
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

                maxBins = Integer.parseInt(prop.getProperty("maxBins"));
                iterations = Integer.parseInt(prop.getProperty("iterations"));
                lossType = prop.getProperty("lossType");
                maxDepth = Integer.parseInt(prop.getProperty("maxDepth"));
                impurity = prop.getProperty("impurity");
                stepSize = Double.parseDouble(prop.getProperty("stepSize"));
                subsamplingRate = Double.parseDouble(prop.getProperty("subsamplingRate"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {  //default values
            maxBins = MAXBINS;
            iterations = ITERATIONS;
            lossType = LOSSTYPE;
            maxDepth =MAXDEPTH;
            impurity =IMPURITY;
            stepSize =STEPSIZE;
            subsamplingRate =SUBSAMPLINGRATE;
        }
    }

    private void initializeModel(Dataset<Row> data){
        if(isToLoad){
            gbtModel=GBTRegressionModel.load(loadModelFolder+"\\"+marketName);
        }
        else{
            readConfigFile();
            gbt = new GBTRegressor()
                .setMaxBins(maxBins)
                .setMaxIter(iterations)
                .setMaxDepth(maxDepth)
                .setImpurity(impurity)
                .setLossType(lossType)
                .setStepSize(stepSize)
                .setSubsamplingRate(subsamplingRate);

            gbtModel = gbt.fit(data);
        }
    }


    private void saveModel(){
        if(isToSave) {
            try {
                gbtModel.write().overwrite().save(outputFilePath + "\\" + marketName);
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
        else{
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
        double aproxPriceChange=calculatePriceChange(lastTrainValue,lastPredictedValue);
        String direction=calculateDirection(aproxPriceChange);
        outputFileline.add(marketName);
        outputFileline.add(direction);
        outputFileline.add(String.valueOf(aproxPriceChange));
        outputFileline.add(String.valueOf(fullVolatility));
    }


    public List<String> getOutputFileline() {
        return outputFileline;
    }
}

