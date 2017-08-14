package org.pentaho.di.jobentry.forexprediction.functions;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class RSI {

    private int numberOfDays;
    private double[][] dataset;
    private List<Double> rsiValues=new ArrayList<>();

    public RSI(double[][] d, int numberDays) {
        dataset=d;
        numberOfDays=numberDays;
    }

    public void calculateRsi(){
        double[] closePrice = new double[dataset.length];
        double[] out = new double[dataset.length];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        for (int i = 0; i < dataset.length; i++) {
            List<Double> copyList = DoubleStream.of(dataset[i]).mapToObj(Double::valueOf).collect(Collectors.toList());
            closePrice[i] = copyList.get(0);
        }
        Core c = new Core();
        RetCode retCode = c.rsi(0, closePrice.length - 1, closePrice, numberOfDays, begin, length, out);
        if (retCode == RetCode.Success) {
            int i=0;
            while (i<=numberOfDays-1) {
                rsiValues.add(closePrice[i]);
                i++;

            }
            for (i = begin.value; i <= out.length-1; i++) {
                rsiValues.add(out[i-begin.value]);
            }
        }
        addEmaToDataset();
    }

    private void addEmaToDataset(){
        for (int i = 0; i < dataset.length; i++) {
            List<Double> copyList = DoubleStream.of(dataset[i]).mapToObj(Double::valueOf).collect(Collectors.toList());
            copyList.add(rsiValues.get(i));
            dataset[i] = copyList.stream().mapToDouble(Double::doubleValue).toArray();
        }
    }

    public double[][] getDataset(){
        return dataset;
    }

}