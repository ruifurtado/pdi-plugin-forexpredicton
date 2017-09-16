<a href="https://www.pentaho.com/">
    <img src="https://trello-attachments.s3.amazonaws.com/59354adb4d8effac88d0b57c/59a53d0198c33653a118f940/95e151ea95ba2f020beda2612ed1212a/pentaho-HGC-logo.png" align="right" height="50" />
</a>

# pdi-plugin-forexprediction

A kettle plugin to compare different FOREX markets, regarding a specific forecast window selected by the user. A [Spark ML lib](https://spark.apache.org/mllib/) Random Forest model is used to predict each market value. [Technical Analysis](https://en.wikipedia.org/wiki/Technical_analysis) is used as feature generator tool, using [TA-Lib](https://github.com/BYVoid/TA-Lib) 
<br><br>
![dialog](https://user-images.githubusercontent.com/11192624/29835337-95c404f6-8ce9-11e7-8539-0bba8250ca88.png)

## Table of content

- [Pre-requisites](#pre-requisites)
- [Installation](#installation)
- [Plugin usage](#plugin-usage)
- [Plugin output](#plugin-output) 
- [To be implemented](#to-be-implemented)

## Pre-requisites 
* Maven, version 3+
* Java JDK 1.8
* This [settings.xml](https://github.com/pentaho/maven-parent-poms/blob/master/maven-support-files/settings.xml) 
in your <user-home>/.m2 directory

## Installation

This part refers to the installation of the forexprediction plugin in the [PDI](https://github.com/pentaho/pentaho-kettle) platform.

* Once the repository is cloned/downloaded, go to the extracted main folder to build the plugin
```bash
cd pdi-plugin-forexpredicton-master
mvn package
```
* After building the plugin, a zip file will be generated in the target folder. Go to that folder and unzip the created file.
```bash
cd assemblies/plugin/target/
unzip kettle-forexpredicton-plugin-8.0-SNAPSHOT.zip
```
* After unzipping the generated plugin, copy the generated file into the plugins folder, available in the Pentaho-kettle  [PDI](https://github.com/pentaho/pentaho-kettle) project.

* The Forex Prediction plugin should now be available in the Spoon application as a Job Step under the Big Data plugin section.

## Plugin usage

As stated before, the pdi-plugin-forexprediction is a forecasting plugin that as the ability to compare different types of FOREX markets. Therefore, there are some specific guidelines that one should follow in order to succesfully run the plugin. Each field of the plugin dialog is explained below:

* __Input folder path__: The plugin receives __N__ CSV's, which correspond to __N__ different markets that user wants to evaluate. To provide this data as input, the user needs to have all the CSV's stored in the same folder, and specify this folder path in the plugin dialog. The specified folder should only contain CSV's that are going to be used by the plugin. The inputed CSV's should be properly formatted as the ones provided in the *sample* folder. The system does not accept non-numerical features, so the user should be careful with the values displayed by each data row. 

* __Output folder path__: An output folder should also be specified by the user. This folder is going to have a __N__ number of CSV's that correspond to individual forecasts of each market. Additionally, an extra CSV will also be provided where each market is compared, along with a market evaluation .TXT file. 

* __Prediction Steps__: This parameter specifies how many days are we going to predict. 

* __Save created models__: If save created model is checked, each trained model will be saved in the selected output folder. The models are saved as a folder with the name of the CSV that was used to train that model.

* __Models file path__: If the user already has some pre-trained models corresponding to each inputed CSV, it is also possible to use them by a similar mechanism as the one used to specify the output folder path, avoiding the training phase. The user must provide a path to a folder where each pre-trained model is stored. Each model should have the same name as the correponding CSV, otherwise the system would not be able to associate them, and will perform unnecessary training.

* __Feature config file__: It's possible to specify the model hyperparameters by providing the path to a simple Java Properties file. A sample file called __config.properties__, is available at *pdi-plugin-forexpredicton-master\assemblies\plugin\src\main\resources*. It's also possible to tweek the number of days that each Technical Indicator considers for it's calculation. If a .properties file is not specified by the user, the system will use pre-defined default values.

## Plugin output

The system produces a different variety of files as outputs: 

* __Output file__: The output file is the file that makes a comparision between different markets. Each market is ranked according to volatility regarding the number of forecasted days. The file displays as metrics the market trend __direction__, the __aprox price change__, the __volatility__ and of course the __rank__ of each market. Each given parameter is calculated regarding the results displayed by each individual __market ouput file__ explained below.

* __Market output file__: As stated previously, when the plugins stops, an individual file is created with metrics regarding each market. This file is created under the name __####OutputFile.csv__, where __####__ is the name of the market, extracted from the original CSV name. This file should be used as a complement to the information displayed in the general __OutputFile.csv__ where each market is ranked according volatility. Its output has daily information regarding the size of the prediction selected by the user. The available metrics are the daily __prediction__, the market trend __direction__, the percentage of __price change__, and again the __volatility__ in percentage but this time with respect to the last 10 days. The size of the file in terms of rows is always the size of the prediction selected by the user x2. The extra rows will be used to evaluate the market.

* __Evaluation file__: To give to the user some information of how good were the model predictions, an evaluation .TXT file was created. This file displays classical regression evaluation metrics, such as the __RMSE__, __MAE__, __R^2__ and __MSE__. We also provide the highest and lowest value of the market.

There is also a python script called __plot.py__ that is available at *pdi-plugin-forexpredicton-master\assemblies\plugin\src\main\resources*. This script could be used to get a visual inspection of how well the model is performing in each market. The script will ask for two CSV's: one market output file, and the original market file (the one without predictions). The plot should be similar to the one presented below. It is perfectly possible to use another plotting tool. 

<br><br>
![plot](https://user-images.githubusercontent.com/11192624/30513532-bf6f76de-9afc-11e7-9daf-0ca2d94fec78.png)

The orange and the green line represent predictions made by the model, the actual forecast and evalaution respectively. The blue line represents the market values from the original CSV. 

