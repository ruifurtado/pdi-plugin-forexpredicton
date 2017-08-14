package org.pentaho.di.jobentry.forexprediction.functions;


import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.ArrayList;
import java.util.List;

public class Volatilty {

    private List<Double> predictedList =new ArrayList<>();
    private List<Double> pcx=new ArrayList<>();
    private List<Double> volatility =new ArrayList<>();

    private List<Double> fullPredictedList= new ArrayList<>();
    private List<Double> fullPcx= new ArrayList<>();
    private double fullVolatility;

    private List<Row> trainset=new ArrayList<>();
    private List<Row> predictions=new ArrayList<>();
    private int pred_size;
    private double lastTrainValue;
    private double lastPredictedValue;

    public Volatilty(Dataset<Row> trainset, Dataset<Row> predictions){
        this.trainset=cleanTrainDataset(trainset);
        this.predictions=cleanPredDataset(predictions);
        pred_size=predictions.collectAsList().size();
    }

    private List<Row> cleanTrainDataset(Dataset<Row> data){
        Dataset<Row> dataset;
        dataset=data.drop("features");
        return dataset.collectAsList();
    }

    private List<Row> cleanPredDataset(Dataset<Row> data){
        Dataset<Row> dataset;
        dataset=data.drop("label");
        dataset=dataset.drop("features");
        return dataset.collectAsList();
    }

    public void calculateVolatility(){
        for(int i=0;i<trainset.size();i++){
            predictedList.add(trainset.get(i).getDouble(trainset.get(i).fieldIndex("label")));
        }
        for(int i=0;i<predictions.size();i++){
            predictedList.add(predictions.get(i).getDouble(predictions.get(i).fieldIndex("prediction")));
        }
        for(int i=0;i<predictedList.size();i++){
            double d=0;
            if(i==0)
                pcx.add(d);
            else
                d=((predictedList.get(i)-predictedList.get(i-1))/predictedList.get(i-1))*100;
                pcx.add(d);
        }
        double[] predArray = pcx.stream().mapToDouble(Double::doubleValue).toArray();
        double [] out=new double[predictedList.size()];
        final int PERIODS_AVERAGE = 10;
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        Core c = new Core();
        RetCode retCode = c.stdDev(0, predArray.length - 1, predArray, PERIODS_AVERAGE,1, begin, length, out);
        if (retCode == RetCode.Success) {

            for (int i = begin.value; i <= out.length-1; i++) {
                volatility.add(out[i-begin.value]*Math.sqrt(252));
            }
        }
        else {
            System.out.println("Error");
        }
    }

    public void calculatePredVolatility(){
        for(int i=0;i<predictions.size();i++) {
            fullPredictedList.add(predictions.get(i).getDouble(predictions.get(i).fieldIndex("prediction")));
        }
        for(int i=0;i<fullPredictedList.size();i++){
            double d=0;
            if(i==0) {
                lastTrainValue=trainset.get(trainset.size()-1).getDouble(trainset.get(trainset.size()-1).fieldIndex("label"));
                lastPredictedValue=fullPredictedList.get(fullPredictedList.size()-1);
                d=((fullPredictedList.get(i)-lastTrainValue)/lastTrainValue)*100;
                fullPcx.add(d); }
            else {
                d=((fullPredictedList.get(i)-fullPredictedList.get(i-1))/fullPredictedList.get(i-1))*100;
                fullPcx.add(d); }
        }
        double [] predArray = fullPcx.stream().mapToDouble(Double::doubleValue).toArray();
        double [] out=new double[fullPredictedList.size()];
        final int PERIODS_AVERAGE = fullPcx.size();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        Core c = new Core();
        RetCode retCode = c.stdDev(0, predArray.length - 1, predArray, PERIODS_AVERAGE,1, begin, length, out);
        if (retCode == RetCode.Success) {

            for (int i = begin.value; i <= out.length-1; i++) {
                fullVolatility=out[i - begin.value] * Math.sqrt(252);
            }
        }
        else {
            System.out.println("Error");
        }
    }



    public List<Double> getVol(){
        List<Double> predVol=new ArrayList<>();
        for(int i=volatility.size()-1;i>volatility.size()-1-pred_size;i--){
            predVol.add(volatility.get(i));
        }
        return predVol;
    }

    public List<Double> getPcx(){
        List<Double> predVol=new ArrayList<>();
        for(int i=pcx.size()-1;i>pcx.size()-1-pred_size;i--){
           predVol.add(pcx.get(i));
        }
        return predVol;
    }

    public double getFullVolatility() {
        return fullVolatility;
    }

    public double getLastTrainValue(){
        return lastTrainValue;
    }

    public double getLastPredictedValue(){
        return lastPredictedValue;
    }
}
