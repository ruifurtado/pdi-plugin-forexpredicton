package org.pentaho.di.jobentry.forexprediction;

import org.apache.spark.sql.Row;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OutputFileMaker {

    private static final int NAME=0;
    private static final int DIRECTION=1;
    private static final int APROXCHANGE=2;
    private static final int VOLATILITY=3;

    private List<List<String>> outputFileList=new ArrayList<>();
    private List<List<String>> upList = new ArrayList<>();
    private List<List<String>> downList =new ArrayList<>();
    private List<List<String>> sortedUpList = new ArrayList<>();
    private List<List<String>> sortedDownList =new ArrayList<>();

    private String outputFilePath;

    public OutputFileMaker(List<List<String>> outputFileList, String outputFilePath){
        this.outputFileList=outputFileList;
        this.outputFilePath=outputFilePath;
    }

    public void writeOutputTextFile() throws IOException {
        File outputFile=new File(outputFilePath,"OutputFile.csv");
        outputFile.delete();
        outputFile.createNewFile();
        String l="NAME"+";"+"DIRECTION"+";"+"APROX. CHANGE %"+";"+"VOLATILITY %"+";"+"RANK"+"\n";
        Files.write(outputFile.toPath(), l.getBytes(), StandardOpenOption.WRITE);
        for(List<String> line:outputFileList){
            if(line.get(DIRECTION).equals("DOWN")) {
                downList.add(line);
            }
            else{
                upList.add(line);
            }
        }
        sortedDownList= sorter(downList);
        sortedUpList=sorter(upList);
        int rank=1;
        for(List<String> up:sortedUpList){
            String line=up.get(NAME)+";"+up.get(DIRECTION)+";"+up.get(APROXCHANGE)+";"+up.get(VOLATILITY)+";"+rank+"\n";
            Files.write(outputFile.toPath(), line.getBytes(), StandardOpenOption.APPEND);
            rank++;
        }
        for(List<String> down:sortedDownList){
            String line=down.get(NAME)+";"+down.get(DIRECTION)+";"+down.get(APROXCHANGE)+";"+down.get(VOLATILITY)+";"+rank+"\n";
            Files.write(outputFile.toPath(), line.getBytes(), StandardOpenOption.APPEND);
            rank++;
        }
    }

    private List<List<String>> sorter(List<List<String>> list){
        List<Double> volList=new ArrayList<>();
        List<List<String>> newList=new ArrayList<>();
        for(List<String> line: list) {
            volList.add(Double.parseDouble(line.get(VOLATILITY)));
            if(line.get(DIRECTION).equals("DOWN"))
                Collections.sort(volList);
            if(line.get(DIRECTION).equals("UP"))
                Collections.sort(volList);
                Collections.reverse(volList);
        }
        for (int i=0;i<volList.size();i++) {
            for(int j=0;j<list.size();j++) {
                if(Double.parseDouble(list.get(j).get(VOLATILITY))==volList.get(i))
                    newList.add(list.get(j));
            }
        }
        return newList;
    }
}
