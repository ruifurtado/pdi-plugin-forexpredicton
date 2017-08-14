package org.pentaho.di.jobentry.forexprediction.functions;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class MACD {
    private int fastPeriod;
    private int slowPeriod;
    private int numberOfDays;
    private double[][] dataset;
    private List<Double> macdValues=new ArrayList<>();

    public MACD(double[][] d, int fastPeriod, int slowPeriod, int numberOfDays) {
        dataset=d;
        this.fastPeriod=fastPeriod;
        this.slowPeriod=slowPeriod;
        this.numberOfDays=numberOfDays;

    }

    public void calculateMacd(){
        double[] closePrice = new double[dataset.length];
        double[] outMacd = new double[dataset.length];
        double[] outMacdSignal = new double[dataset.length];
        double[] outMacdHist = new double[dataset.length];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        for (int i = 0; i < dataset.length; i++) {
            List<Double> copyList = DoubleStream.of(dataset[i]).mapToObj(Double::valueOf).collect(Collectors.toList());
            closePrice[i] = copyList.get(0);
        }
        Core c = new Core();
        RetCode retCode =c.macd(0,closePrice.length-1,closePrice,fastPeriod,slowPeriod,numberOfDays,begin,length,outMacd,outMacdSignal,outMacdHist);
        if (retCode == RetCode.Success) {
            int i=0;
            while (i<slowPeriod-1+numberOfDays-1) {
                macdValues.add(closePrice[i]);
                i++;
            }
            for (i = begin.value; i <= outMacdHist.length-1; i++) {
                macdValues.add(outMacdHist[i-begin.value]);
            }
        }
        addMacdToDataset();
    }

    private void addMacdToDataset(){
        for (int i = 0; i < dataset.length; i++) {
            List<Double> copyList = DoubleStream.of(dataset[i]).mapToObj(Double::valueOf).collect(Collectors.toList());
            copyList.add(macdValues.get(i));
            dataset[i] = copyList.stream().mapToDouble(Double::doubleValue).toArray();
        }
    }

    public double[][] getDataset(){
        return dataset;
    }
}
