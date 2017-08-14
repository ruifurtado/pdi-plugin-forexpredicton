package org.pentaho.di.jobentry.forexprediction.functions;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class MOM {
    private int numberOfDays;
    private double[][] dataset;
    private List<Double> momValues=new ArrayList<>();

    public MOM(double[][] d, int numberDays) {
        dataset=d;
        numberOfDays=numberDays;
    }

    public void calculateMom(){
        double[] closePrice = new double[dataset.length];
        double[] out = new double[dataset.length];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        for (int i = 0; i < dataset.length; i++) {
            List<Double> copyList = DoubleStream.of(dataset[i]).mapToObj(Double::valueOf).collect(Collectors.toList());
            closePrice[i] = copyList.get(0);
        }
        Core c = new Core();
        RetCode retCode = c.mom(0, closePrice.length - 1, closePrice, numberOfDays, begin, length, out);
        if (retCode == RetCode.Success) {
            int i=0;
            while (i<=numberOfDays-1) {
                momValues.add(closePrice[i]);
                i++;
            }
            for (i = begin.value; i <= out.length-1; i++) {
                momValues.add(out[i-begin.value]);
            }
        }
        addMomToDataset();
    }

    private void addMomToDataset(){
        for (int i = 0; i < dataset.length; i++) {
            List<Double> copyList = DoubleStream.of(dataset[i]).mapToObj(Double::valueOf).collect(Collectors.toList());
            copyList.add(momValues.get(i));
            dataset[i] = copyList.stream().mapToDouble(Double::doubleValue).toArray();
        }
    }

    public double[][] getDataset(){
        return dataset;
    }

}
