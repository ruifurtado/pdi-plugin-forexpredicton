<a href="https://www.pentaho.com/">
    <img src="https://businessintelligence.com/wp-content/themes/bi/assets/images/vendor/pentaho-logo.png" align="right" height="110" />
</a>

# pdi-plugin-forexprediction

A kettle plugin to forecast and compare how a set of different FOREX markets will be in a near future.
<br><br>
![dialog](https://user-images.githubusercontent.com/11192624/29817759-29d8a2fc-8cb1-11e7-9acd-d24494f51ebf.png)

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

As stated before, the pdi-plugin-forexprediction is a forecasting plugin that as the ability to compare different types of FOREX markets. Therefore, there are some specific guidelines that one should follow in order to succesfully run the plugin.

* The plugin receives an *__n__*
