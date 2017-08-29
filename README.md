<a href="https://www.pentaho.com/">
    <img src="https://businessintelligence.com/wp-content/themes/bi/assets/images/vendor/pentaho-logo.png" align="right" height="110" />
</a>

# pdi-plugin-forexprediction

A kettle plugin to forecast and compare how a set of different FOREX markets will be in a near future. [Technical Analysis](https://en.wikipedia.org/wiki/Technical_analysis) is used as feature generator tool and [Spark ML lib](https://spark.apache.org/mllib/) as the selected Machine Learning library.
<br><br>
![dialog](https://user-images.githubusercontent.com/11192624/29835337-95c404f6-8ce9-11e7-8539-0bba8250ca88.png)

## Table of content

- [Pre-requisites](#pre-requisites)
- [Installation](#installation)
- [Plugin usage](#plugin-usage)
- [Plugin components](#plugin-components)
  - [input](#input)
  - [output](#output)
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

* __Input folder path__: The plugin receives *__N__* CSV, which correspond to *__N__* different markets that user wants to evaluate. To provide this data as input, the user needs to have all the CSV's stored in the same folder, and specify this folder path in the plugin dialog. The specified folder should only contain CSV's that are going to be used by the plugin. The inputed CSV's should be properly formatted as the ones provided in the __sample__ folder. The system does not accept non-numerical features, so the user should be careful with the values displayed by each data row. 

* __Output folder path__: An output folder should also be specified by the user. This folder is going to have a *__N__* number of CSV's that correspond to individual forecasts of each market. Additionally, an extra CSV will also be provided where each market is compared.

* __Prediction Steps__: This parameter specifies how many days are we going to predict. 

* __Save created models__: If save created model is checked, each trained model will be saved in the selected output folder. The models are saved as a folder with the name of the CSV that was used to train that model.

* __Models file path__: If the user already has some pre-trained models corresponding to each inputed CSV, it is also possible to use them by a similar mechanism as the one used to specify the output folder path, avoiding the training phase. The user must provide a path to a folder where each pre-trained model is stored. Each model should have the same name as the correponding CSV, otherwise the system would not be able to associate them, and will perform unnecessary training.

* __Feature config file__: It's possible to specify the model hyperparameters by providing the path to a simple Java Properties file. A sample file called *config.properties*, is available at pdi-plugin-forexpredicton-master\assemblies\plugin\src\main\resources. If a .properties file is not specified by the user, the system will use pre-defined default values.
