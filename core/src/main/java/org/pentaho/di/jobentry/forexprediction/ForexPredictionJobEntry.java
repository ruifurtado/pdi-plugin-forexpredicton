/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

package org.pentaho.di.jobentry.forexprediction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

@JobEntry(
        id = "ForexPrediction.Name",
        name = "ForexPrediction.Name",
        description = "ForexPrediction.TooltipDesc",
        image = "icon.svg",
        categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.BigData",
        i18nPackageName = "org.pentaho.di.jobentries.forexprediction"
)
public class ForexPredictionJobEntry extends JobEntryBase implements Cloneable, JobEntryInterface {
    private static Class<?> PKG = ForexPredictionJobEntry.class; // for i18n purposes $NON-NLS-1$

    private String filename;
    private String output;
    private String forecastSteps;
    private boolean toLoad;
    private String toLoadFile;
    private boolean toSave;
    private String modelName;
    private List<String> filesList=new ArrayList<>();
    private String configFeaturesFile;

    private static String DEFAULT_FORECAST_STEPS = "3";

    public ForexPredictionJobEntry(String name ) {
        super( name, "" );
        filename = null;
        output = null;
        forecastSteps = DEFAULT_FORECAST_STEPS;
        toLoad = false;
        toLoadFile = null;
        toSave = false;
        modelName = null;
        configFeaturesFile=null;
    }

    public ForexPredictionJobEntry() {
        this( "" );
    }


    public Object clone() {
        ForexPredictionJobEntry je = (ForexPredictionJobEntry) super.clone();
        return je;
    }



    private static abstract class FIELDS {
        static final String filename = "filename";
        static final String output = "output";
        static final String forecastSteps = "forecastSteps";
        static final String toLoad = "toLoad";
        static final String toLoadFile = "toLoadFile";
        static final String toSave = "toSave";
        static final String modelName = "modelName";
        static final String configFeaturesFile="configFeaturesFile";
    }
    @Override
    public String getXML() {
        StringBuffer retval = new StringBuffer( 200 );

        retval.append( super.getXML() );
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.filename, Const.nullToEmpty( getFilename() ) ));
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.output, Const.nullToEmpty( getOutput() ) ));
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.forecastSteps, Const.nullToEmpty( getForecastSteps() ) ));
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.toLoad, isToLoad() ));
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.toLoadFile, Const.nullToEmpty( getToLoadFile() ) ));
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.toSave, isToSave() ));
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.modelName, getModelName() ));
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.configFeaturesFile, getConfigFeaturesFile() ));
        return retval.toString();
    }

    @Override
    public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep, IMetaStore metaStore ) throws KettleXMLException {
        try {
            super.loadXML( entrynode, databases, slaveServers );
            setFilename( XMLHandler.getTagValue(entrynode, FIELDS.filename ) );
            setOutput( XMLHandler.getTagValue(entrynode, FIELDS.output) );
            setForecastSteps( XMLHandler.getTagValue(entrynode, FIELDS.forecastSteps ) );
            setToLoad( "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, FIELDS.toLoad)) );
            setToLoadFile( XMLHandler.getTagValue(entrynode, FIELDS.toLoadFile) );
            setToSave( "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, FIELDS.toSave)) );
            setModelName( XMLHandler.getTagValue(entrynode, FIELDS.modelName ) );
            setConfigFeaturesFile(XMLHandler.getTagValue(entrynode, FIELDS.configFeaturesFile ));
        } catch ( Exception e ) {
            throw new KettleXMLException( "Unable to load arguments from XML node" , e );
        }
    }

    @Override
    public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
                         List<SlaveServer> slaveServers ) throws KettleException {
        try {
            setFilename(rep.getJobEntryAttributeString( id_jobentry, FIELDS.filename ));
            setOutput(rep.getJobEntryAttributeString(id_jobentry, FIELDS.output ));
            setForecastSteps(rep.getJobEntryAttributeString( id_jobentry, FIELDS.forecastSteps ));
            setToLoad(rep.getJobEntryAttributeBoolean( id_jobentry, FIELDS.toLoad ));
            setToLoadFile(rep.getJobEntryAttributeString( id_jobentry, FIELDS.toLoadFile ));
            setToSave(rep.getJobEntryAttributeBoolean( id_jobentry, FIELDS.toSave ));
            setModelName(rep.getJobEntryAttributeString( id_jobentry, FIELDS.modelName ));
            setConfigFeaturesFile(rep.getJobEntryAttributeString( id_jobentry, FIELDS.configFeaturesFile ));
        } catch ( Exception dbe ) {
            throw new KettleException(
                    "Unable to load job entry of type 'DeepForecast' from the repository for id_jobentry=" + id_jobentry, dbe );
        }
    }

    @Override
    public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
        try {
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.filename, getFilename() );
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.output, getOutput() );
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.forecastSteps, getForecastSteps() );
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.toLoad, isToLoad() );
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.toLoadFile, getToLoadFile() );
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.toSave, isToSave() );
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.modelName, getModelName() );
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.configFeaturesFile, getConfigFeaturesFile() );
        } catch ( Exception dbe ) {
            throw new KettleException( "Unable to save job entry of type 'DeepForecast' to the repository for id_job="
                    + id_job, dbe );
        }
    }


    /**
     * This method is called when it is the job entry's turn to run during the execution of a job.
     * It should return the passed in Result object, which has been updated to reflect the outcome
     * of the job entry. The execute() method should call setResult(), setNrErrors() and modify the
     * rows or files attached to the result object if required.
     *
     * @param result The result of the previous execution
     * @return The Result of the execution.
     */
    public Result execute( Result result, int nr ) {
        result.setNrErrors( 0 );
        result.setResult(true);

        logBasic("check");

        String realFolderFilename = environmentSubstitute(getFilename());
        String realOutput =  environmentSubstitute(getOutput());
        String realForecastSteps = environmentSubstitute(getForecastSteps());
        String realToLoadFile = null;
        String realModelName = null;
        String realConfigFeaturesFile = environmentSubstitute(getConfigFeaturesFile());
        if (isToLoad()) {
            realToLoadFile = environmentSubstitute(getToLoadFile());
        }
        if (isToSave()) {
            realModelName = environmentSubstitute(getModelName());
        }

        List<List<String>> outputFileList=new ArrayList<>();

        try {
            filesInFolder(realFolderFilename);
            for(int i=0;i<filesList.size();i++){
                DatasetManager data=new DatasetManager(filesList.get(i),realForecastSteps,realOutput,realConfigFeaturesFile);
                data.fillDataset();
                TimeSeriesSparkPrediction ts=new TimeSeriesSparkPrediction(filesList.get(i),realOutput,realToLoadFile,toSave,toLoad,i,realConfigFeaturesFile);
                ts.TimeSeriesPredict();
                outputFileList.add(ts.getOutputFileline());
            }
            OutputFileMaker out=new OutputFileMaker(outputFileList,realOutput);
            out.writeOutputTextFile();

        } catch (Exception e) {
            result.setNrErrors( 1 );
            result.setResult(false);
            e.printStackTrace();
            logError(toString(), "Error processing ForexPredictionJob: " + e.getMessage());
        }

        return result;
    }

    private void filesInFolder(String f){
        File folder = new File(f);
        File[] listOfFiles=folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".csv")) {
                filesList.add(listOfFiles[i].getAbsolutePath());
            }
        }
    }

    @Override
    public boolean evaluates() {
        return true;
    }

    @Override
    public boolean isUnconditional() {
        return true;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename( String filename ) {
        this.filename = filename;
    }

    public String getForecastSteps() {
        return forecastSteps;
    }

    public void setForecastSteps(String forecastSteps) {
        this.forecastSteps = forecastSteps;
    }

    public boolean isToLoad() {
        return toLoad;
    }

    public void setToLoad(boolean toLoad) {
        this.toLoad = toLoad;
    }

    public String getToLoadFile() {
        return toLoadFile;
    }

    public void setToLoadFile(String toLoadFile) {
        this.toLoadFile = toLoadFile;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public boolean isToSave() {
        return toSave;
    }

    public void setToSave(boolean toSave) {
        this.toSave = toSave;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getConfigFeaturesFile() {
        return configFeaturesFile;
    }

    public void setConfigFeaturesFile(String configFeaturesFile) {
        this.configFeaturesFile = configFeaturesFile;
    }

}
