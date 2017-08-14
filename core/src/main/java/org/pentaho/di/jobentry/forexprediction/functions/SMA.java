package org.pentaho.di.jobentry.forexprediction.functions;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class SMA {

    private int numberOfDays;
    private double[][] dataset;
    private List<Double> smaValues=new ArrayList<>();

    public SMA(double[][] d, int numberDays) {
        dataset=d;
        numberOfDays=numberDays;
    }

    public double[][] getDataset(){
        return dataset;
    }

    public void calculateSma() {
        double[] closePrice = new double[dataset.length];
        double[] out = new double[dataset.length];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        for (int i = 0; i < dataset.length; i++) {
            List<Double> copyList = DoubleStream.of(dataset[i]).mapToObj(Double::valueOf).collect(Collectors.toList());
            closePrice[i] = copyList.get(0);
        }
        Core c = new Core();
        RetCode retCode = c.sma(0, closePrice.length - 1, closePrice, numberOfDays, begin, length, out);
        if (retCode == RetCode.Success) {
            int i=0;
            while (i<numberOfDays-1) {
                smaValues.add(closePrice[i]);
                i++;
            }
            for (i = begin.value; i <= out.length-1; i++) {
                smaValues.add(out[i-begin.value]);
            }
        }
        addSmaToDataset();
    }

    private void addSmaToDataset(){
        for (int i = 0; i < dataset.length; i++) {
            List<Double> copyList = DoubleStream.of(dataset[i]).mapToObj(Double::valueOf).collect(Collectors.toList());
            copyList.add(smaValues.get(i));
            dataset[i] = copyList.stream().mapToDouble(Double::doubleValue).toArray();
        }
    }
}
