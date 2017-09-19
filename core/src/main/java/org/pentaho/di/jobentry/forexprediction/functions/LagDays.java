package org.pentaho.di.jobentry.forexprediction.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class LagDays {

    private double[][] dataset;
    private List<Double> yesterdayValues=new ArrayList<>();
    private List<Double> twoDaysValues=new ArrayList<>();
    private List<Double> threeDaysValues=new ArrayList<>();

    public LagDays(double[][] d) {
        dataset=d;
    }

    public void calculateLagDays(){
        for (int i = 0; i < dataset.length; i++) {
            List<Double> copyList = DoubleStream.of(dataset[i]).mapToObj(Double::valueOf).collect(Collectors.toList());
            yesterdayValues.add(copyList.get(0));
            twoDaysValues.add(copyList.get(0));
            threeDaysValues.add(copyList.get(0));
        }
        yesterdayValues=lagArray(1,yesterdayValues);
        twoDaysValues=lagArray(2,twoDaysValues);
        threeDaysValues=lagArray(3,threeDaysValues);

        addLaggedDaysToDataset(yesterdayValues);
        addLaggedDaysToDataset(twoDaysValues);
        addLaggedDaysToDataset(threeDaysValues);

    }

    public List<Double> lagArray(int lag, List<Double> values){
        int i=0;
        List<Double> answer=new ArrayList<>();
        answer=values.subList(lag,values.size());
        while(i<lag){
            answer.add(0,0.0);
            i++;
        }
        return values;
    }

    private void addLaggedDaysToDataset(List<Double> values){
        for (int i = 0; i < dataset.length; i++) {
            List<Double> copyList = DoubleStream.of(dataset[i]).mapToObj(Double::valueOf).collect(Collectors.toList());
            copyList.add(values.get(i));
            dataset[i] = copyList.stream().mapToDouble(Double::doubleValue).toArray();
        }
    }

    public double[][] getDataset(){
        return dataset;
    }
}
