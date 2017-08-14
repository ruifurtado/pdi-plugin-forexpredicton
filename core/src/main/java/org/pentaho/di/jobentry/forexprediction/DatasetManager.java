package org.pentaho.di.jobentry.forexprediction;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.pentaho.di.jobentry.forexprediction.functions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.*;


public class DatasetManager {  //Format Date,EUR/USD Close,EUR/USD High,EUR/USD Low

    private static final int SMADAYS = 10;
    private static final int EMADAYS = 22;
    private static final int WMADAYS = 20;
    private static final int MOMDAYS = 12;
    private static final int MACDDAYS = 12;
    private static final int RSIDAYS = 26;
    private static final int MACDFAST = 9;
    private static final int MACDSLOW = 14;
    private double[][] dataset;   //A info esta guardada com close;high;low
    private double[] labelset;
    private List<String> datasetDates=new ArrayList<>();
    private List<String> hyperparameters=new ArrayList<>();
    private List<Integer> daysComparer=new ArrayList<>();
    private String csvFile;
    private EMA ema;
    private File file;
    private File file2;
    private SMA sma;
    private WMA wma;
    private MOM mom;
    private MACD macd;
    private RSI rsi;
    private LabelMaker labelCreator;
    private int numberOfLines;
    private File baseDir;
    private int numberOfDaysToPredict;
    private String outputFolderPath;
    private String configFeaturesFile;
    private int smaDays,emaDays,wmaDays,momDays,macdDays,rsiDays,macdFast,macdSlow;

    public DatasetManager(String filename, String numberOfDaysToPredict, String outputFolderPath, String configFeaturesFile) {
        this.csvFile = filename;
        this.numberOfDaysToPredict=Integer.parseInt(numberOfDaysToPredict);
        this.baseDir = new File( new File(filename).getParent());
        this.outputFolderPath=outputFolderPath;
        this.configFeaturesFile=configFeaturesFile;
    }

    private void datasetCreator() throws Exception {
        try {
            int numberOfStringLines = 0;
            countFileLines(csvFile);
            List<double[]> data = new ArrayList<double[]>();
            double[] row;
            int c = 0;
            String j;
            Scanner in = new Scanner(new File(csvFile));
            while (in.hasNextLine()) {
                c = c + 1;
                String line = in.nextLine();
                String[] rowList = line.split(",");

                for (int i = 0; i < rowList.length; i++) {  //
                    row = new double[rowList.length - 1];
                    if (rowList[i].contains(j = ";") || rowList[i].contains(j = "\"")) {
                        rowList[i] = rowList[i].replaceAll(j, "");
                    }
                }
                if (NumberUtils.isCreatable(rowList[1])) {
                    datasetDates.add(rowList[0]);
                    String[] rowListCopy = new String[rowList.length - 1];
                    System.arraycopy(rowList, 1, rowListCopy, 0, rowList.length - 1);
                    row = Arrays.stream(rowListCopy).mapToDouble(Double::parseDouble).toArray();
                    data.add(row);
                } else {
                    numberOfStringLines++;
                }
            }
            dataset = new double[numberOfLines - numberOfStringLines][];
            fromListToDoubleArray(data);
            readConfigFile();
        }
        catch (Exception e){
           throw new Exception("Error creating the dataset: " + e.getMessage());
        }
    }

    private void countFileLines(String filename) throws Exception {
        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader(new File(filename)));
            lnr.skip(Long.MAX_VALUE);
            numberOfLines = lnr.getLineNumber();
            lnr.close();
        }
        catch(Exception e){
            throw new Exception("Error creating the dataset: " + e.getMessage());
        }
    }

    private void fromListToDoubleArray(List<double[]> l) {
        int i = 0;
        for (double[] doubleList : l) {
            if (i != l.size())
                dataset[i++] = doubleList;
        }
    }

    public double[][] getDataset() {
        return dataset;
    }

    public void fillDataset() throws Exception {

        datasetCreator();

        sma = new SMA(dataset, smaDays); //Depois tenho de passar estes dias para constante
        sma.calculateSma();
        dataset = sma.getDataset();

        ema = new EMA(dataset, emaDays);
        ema.calculateEma();
        dataset = ema.getDataset();

        wma = new WMA(dataset, wmaDays);
        wma.calculateWma();
        dataset = wma.getDataset();

        mom = new MOM(dataset, momDays);
        mom.calculateMom();
        dataset = mom.getDataset();

        macd = new MACD(dataset, macdFast,macdSlow,macdDays);
        macd.calculateMacd();
        dataset = macd.getDataset();

        rsi=new RSI(dataset,rsiDays);
        rsi.calculateRsi();
        dataset=rsi.getDataset();

        labelCreator = new LabelMaker(dataset,numberOfDaysToPredict);
        labelCreator.LabelAtribuiton();
        labelset =labelCreator.getLabelset();
        cleaner();


        try {
            writeNewTrainTestCsv(dataset,labelset,numberOfDaysToPredict);
        } catch (Exception e) {
            throw new Exception("Error creating train and test file");
        }
    }

    private double[] getLabelSet() {
        return labelset;
    }

    private void cleaner() {
        int i = 0;
        int max=Collections.max(daysComparer);
        while (i < max) { //26 fast period + 9 ema over MACD minus 2 (1 per period)
            dataset = ArrayUtils.remove(dataset, 0);
            labelset=  ArrayUtils.remove(labelset, 0);
            datasetDates.remove(0);
            i++;
        }
    }

    public void writeNewTrainTestCsv(double [][] d,double [] l,int lookback) throws IOException {
        String append=getNameForFile(csvFile);
        file=new File(outputFolderPath,append+"TrainFile.csv");
        file2=new File(outputFolderPath,append+"TestFile.csv");
        if(file.exists() && file2.exists()){
            FileWriter fw = new FileWriter(file,false);
            FileWriter fw2 = new FileWriter(file2,false);
        }
        else{
            file.createNewFile();
            file2.createNewFile();
        }
        for(int i=0;i<d.length-lookback;i++) {
            String line=datasetDates.get(i)+","+Arrays.toString(d[i]).substring(1,Arrays.toString(d[i]).length()-1).replace(" ","")+","+l[i]+"\n";
            try {
                Files.write(file.toPath(), line.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        for (int j=d.length-lookback;j<d.length;j++){
            String line=datasetDates.get(j)+","+Arrays.toString(d[j]).substring(1,Arrays.toString(d[j]).length()-1).replace(" ","")+"\n";
            try {
                Files.write(file2.toPath(), line.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private String getNameForFile(String filepath){
        String f=new File(filepath).getName();
        String [] splitter =f.split(".csv");
        return splitter[0];
    }

    private void readConfigFile(){
        if(configFeaturesFile!=null) {
            Properties prop = new Properties();
            InputStream input = null;
            try {
                input = new FileInputStream(configFeaturesFile);

                // load a properties file
                prop.load(input);

                // get the property value and print it out
                smaDays = Integer.parseInt(prop.getProperty("SMAdays"));
                emaDays = Integer.parseInt(prop.getProperty("EMAdays"));
                wmaDays = Integer.parseInt(prop.getProperty("WMAdays"));
                momDays = Integer.parseInt(prop.getProperty("MOMdays"));
                macdFast = Integer.parseInt(prop.getProperty("MACDfastPeriod"));
                macdSlow = Integer.parseInt(prop.getProperty("MACDslowPeriod"));
                macdDays = Integer.parseInt(prop.getProperty("MACDdays"));
                rsiDays = Integer.parseInt(prop.getProperty("RSIdays"));

                hyperparameters.add(prop.getProperty("maxBins"));
                hyperparameters.add(prop.getProperty("iterations"));
                hyperparameters.add(prop.getProperty("lossType"));
                hyperparameters.add(prop.getProperty("maxDepth"));
                hyperparameters.add(prop.getProperty("impurity"));
                hyperparameters.add(prop.getProperty("stepSize"));
                hyperparameters.add(prop.getProperty("subsamplingRate"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{  //default values
            smaDays = SMADAYS;
            emaDays =EMADAYS;
            wmaDays = WMADAYS;
            momDays = MOMDAYS;
            macdFast = MACDFAST;
            macdSlow = MACDSLOW;
            macdDays = MACDDAYS;
            rsiDays = RSIDAYS;
        }

        daysComparer.add(smaDays-1);
        daysComparer.add(emaDays-1);
        daysComparer.add(wmaDays-1);
        daysComparer.add(momDays-1);
        daysComparer.add(macdFast-1);
        daysComparer.add(macdSlow+macdDays-2);
        daysComparer.add(rsiDays-1);
    }

    public String getFilePath() {
        return file.getPath();
    }

    public String getFile2Path() {
        return file2.getPath();
    }

    public String getBaseDirPath(){ return baseDir.getPath();}

    public String getOutputFolderPath(){ return outputFolderPath;}
}
