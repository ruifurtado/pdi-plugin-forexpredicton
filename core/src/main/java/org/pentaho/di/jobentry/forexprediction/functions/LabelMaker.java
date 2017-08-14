package org.pentaho.di.jobentry.forexprediction.functions;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class LabelMaker {

    private double[][] dataset;
    private double[] labelset;
    private double[] volLabelset;
    private int lookback;
    private String labelType;

    public LabelMaker (double[][] d, int l) {
        dataset=d;
        lookback=l;
    }

    public void LabelAtribuiton() {
        double todayPrice;
        double tomorrowPrice=0;
        int label = 0;
        int n=0;
        labelset=new double[dataset.length];
        volLabelset=new double[dataset.length];
        for(int i=0;i<dataset.length;i++) {
            List<Double> copyList=DoubleStream.of(dataset[i]).mapToObj(Double::valueOf).collect(Collectors.toList()); //convert double to list
            todayPrice=copyList.get(n);
            if(i<dataset.length-lookback) {
                List<Double> copyList2=DoubleStream.of(dataset[i+lookback]).mapToObj(Double::valueOf).collect(Collectors.toList()); //convert double to list
                tomorrowPrice=copyList2.get(n);
                labelset[i]=tomorrowPrice;
            }
            else {
                labelset[i]=tomorrowPrice;
            }
        }
    }

    public double[] getLabelset(){
        return labelset;
    }

    public double[] getVolLabelset() {
        return volLabelset;
    }

    public int sigmoid(double x) {
        int answer=0;
        if(x<=0)
            answer=0;
        if(x>0)
            answer=1;
        return answer;
    }
}
