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

package org.pentaho.di.ui.jobentry.forexprediction;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.jobentry.forexprediction.ForexPredictionJobEntry;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class ForexPredictionJobEntryDialog extends JobEntryDialog implements JobEntryDialogInterface {
    private static Class<?> PKG = ForexPredictionJobEntry.class; // for i18n purposes
    private static final String[] FILETYPES = new String[] { BaseMessages.getString(PKG, "ForexPrediction.Filetype.All")};
    private Text wName;
    private TextVar wFilename;
    private TextVar wForecastSteps;
    private Button wToSave;
    private TextVar wToLoadFile;
    private TextVar wOutputFolder;
    private TextVar wConfigFile;
    private ForexPredictionJobEntry meta;
    private Shell shell;
    private boolean changed;

    public ForexPredictionJobEntryDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta ) {
        super( parent, jobEntryInt, rep, jobMeta );
        meta = (ForexPredictionJobEntry) jobEntryInt;
        if ( this.meta.getName() == null ) {
            this.meta.setName( BaseMessages.getString( PKG, "ForexPrediction.Name") );
        }
    }

    public JobEntryInterface open() {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
        props.setLook( shell );
        JobDialog.setShellImage( shell, meta );

        ModifyListener lsMod = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                meta.setChanged();
            }
        };
        changed = meta.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout( formLayout );
        shell.setText( BaseMessages.getString(PKG, "ForexPrediction.Shell.Title")  );

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Job entry name line
        Label wlName = new Label(shell, SWT.RIGHT);
        wlName.setText( BaseMessages.getString( PKG, "ForexPrediction.JobEntryName.Label" ) );
        props.setLook(wlName);
        FormData fdlName = new FormData();
        fdlName.left = new FormAttachment( 0, 0 );
        fdlName.right = new FormAttachment( middle, -margin );
        fdlName.top = new FormAttachment( 0, margin );
        wlName.setLayoutData(fdlName);
        wName = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( wName );
        wName.addModifyListener( lsMod );
        FormData fdName = new FormData();
        fdName.left = new FormAttachment( middle, 0 );
        fdName.top = new FormAttachment( 0, margin );
        fdName.right = new FormAttachment( 100, 0 );
        wName.setLayoutData(fdName);

        // Filename line
        Label wlFilename = new Label(shell, SWT.RIGHT);
        wlFilename.setText( BaseMessages.getString( PKG, "ForexPrediction.Filename.Label" ) );
        props.setLook(wlFilename);
        FormData fdlFilename = new FormData();
        fdlFilename.left = new FormAttachment( 0, 0 );
        fdlFilename.top = new FormAttachment( wName, margin );
        fdlFilename.right = new FormAttachment( middle, -margin );
        wlFilename.setLayoutData(fdlFilename);

        Button wbFilename = new Button(shell, SWT.PUSH | SWT.CENTER);
        props.setLook(wbFilename);
        wbFilename.setText( BaseMessages.getString( PKG, "ForexPrediction.Browse" ) );
        FormData fdbFilename = new FormData();
        fdbFilename.right = new FormAttachment( 100, 0 );
        fdbFilename.top = new FormAttachment( wName, 0 );
        wbFilename.setLayoutData(fdbFilename);

        wFilename = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( wFilename );
        wFilename.addModifyListener( lsMod );
        FormData fdFilename = new FormData();
        fdFilename.left = new FormAttachment( middle, 0 );
        fdFilename.top = new FormAttachment( wName, margin );
        fdFilename.right = new FormAttachment(wbFilename, -margin );
        wFilename.setLayoutData(fdFilename);
        wFilename.setToolTipText( BaseMessages.getString( PKG, "ForexPrediction.Filename.Tooltip" ) );

        wbFilename.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                DirectoryDialog dialog=new DirectoryDialog(shell,SWT.OPEN);
                if (wFilename.getText() != null) {
                    dialog.setFilterPath(jobMeta.environmentSubstitute(wFilename.getText()));
                }
                String dir = dialog.open();
                if (dir != null) {
                    wFilename.setText(dir);
                }
            }
        } );

        // output folder
        Label wlOutputFolder = new Label(shell, SWT.RIGHT);
        wlOutputFolder.setText( BaseMessages.getString( PKG, "ForexPrediction.OutputFolder.Label" ) );
        props.setLook(wlOutputFolder);
        FormData fdlOutputFolder = new FormData();
        fdlOutputFolder.left = new FormAttachment( 0, 0 );
        fdlOutputFolder.top = new FormAttachment( wFilename, margin );
        fdlOutputFolder.right = new FormAttachment( middle, -margin );
        wlOutputFolder.setLayoutData(fdlOutputFolder);
        Button wbOutputFolder = new Button(shell, SWT.PUSH | SWT.CENTER);
        props.setLook(wbOutputFolder);
        wbOutputFolder.setText( BaseMessages.getString( PKG, "ForexPrediction.Browse" ) );
        FormData fdbOutputFolder = new FormData();
        fdbOutputFolder.right = new FormAttachment( 100, 0 );
        fdbOutputFolder.top = new FormAttachment( wFilename, margin );
        wbOutputFolder.setLayoutData(fdbOutputFolder);
        wbOutputFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                DirectoryDialog ddialog = new DirectoryDialog( shell, SWT.OPEN );
                if ( wOutputFolder.getText() != null ) {
                    ddialog.setFilterPath( jobMeta.environmentSubstitute( wOutputFolder.getText() ) );
                }
                // Calling open() will open and run the dialog.
                // It will return the selected directory, or
                // null if user cancels
                String dir = ddialog.open();
                if ( dir != null ) {
                    // Set the text box to the new selection
                    wOutputFolder.setText( dir );
                }

            }
        } );
        wOutputFolder = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( wOutputFolder );
        wOutputFolder.addModifyListener( lsMod );
        FormData fdOutputFolder = new FormData();
        fdOutputFolder.left = new FormAttachment( middle, 0 );
        fdOutputFolder.top = new FormAttachment( wFilename, margin );
        fdOutputFolder.right = new FormAttachment(wbOutputFolder, -margin );
        wOutputFolder.setLayoutData(fdOutputFolder);
        wOutputFolder.setToolTipText( BaseMessages.getString( PKG, "ForexPrediction.OutputFolder.Tooltip" ) );

        // Forecast Steps line
        Label wlForecastSteps = new Label(shell, SWT.RIGHT);
        wlForecastSteps.setText( BaseMessages.getString( PKG, "ForexPrediction.ForecastSteps.Label" ) );
        props.setLook(wlForecastSteps);
        FormData fdlForecastSteps = new FormData();
        fdlForecastSteps.left = new FormAttachment( 0, 0 );
        fdlForecastSteps.top = new FormAttachment( wOutputFolder, margin );
        fdlForecastSteps.right = new FormAttachment( middle, -margin );
        wlForecastSteps.setLayoutData(fdlForecastSteps);
        wForecastSteps = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( wForecastSteps );
        wForecastSteps.setToolTipText( BaseMessages.getString( PKG, "ForexPrediction.ForecastSteps.Tooltip" ) );
        wForecastSteps.addModifyListener( lsMod );
        FormData fdForecastSteps = new FormData();
        fdForecastSteps.left = new FormAttachment( middle, 0 );
        fdForecastSteps.top = new FormAttachment( wOutputFolder, margin );
        fdForecastSteps.right = new FormAttachment( 100, 0 );
        wForecastSteps.setLayoutData(fdForecastSteps);

        // save file check
        Label wlToSave = new Label(shell, SWT.RIGHT);
        wlToSave.setText( BaseMessages.getString( PKG, "ForexPrediction.ToSave.Label" ) );
        props.setLook(wlToSave);
        FormData fdlToSave = new FormData();
        fdlToSave.left = new FormAttachment( 0, 0 );
        fdlToSave.top = new FormAttachment( wForecastSteps, margin );
        fdlToSave.right = new FormAttachment( middle, -margin );
        wlToSave.setLayoutData(fdlToSave);
        wToSave = new Button( shell, SWT.CHECK );
        props.setLook( wToSave );
        FormData fdToSave = new FormData();
        fdToSave.left = new FormAttachment( middle, 0 );
        fdToSave.top = new FormAttachment( wForecastSteps, margin );
        fdToSave.right = new FormAttachment( 100, 0 );
        wToSave.setLayoutData(fdToSave);
        wToSave.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                meta.setToSave(!meta.isToSave());
                meta.setChanged();
            }
        } );
        wToSave.setToolTipText(BaseMessages.getString( PKG, "ForexPrediction.ToSave.Tooltip" ) );


        // to load file
        Label wlToLoadFile = new Label(shell, SWT.RIGHT);
        wlToLoadFile.setText( BaseMessages.getString( PKG, "ForexPrediction.ToLoadFile.Label" ) );
        props.setLook(wlToLoadFile);
        FormData fdlToLoadFile = new FormData();
        fdlToLoadFile.left = new FormAttachment( 0, 0 );
        fdlToLoadFile.top = new FormAttachment( wToSave, margin );
        fdlToLoadFile.right = new FormAttachment( middle, 0 );
        wlToLoadFile.setLayoutData(fdlToLoadFile);

        Button wbToLoadFile = new Button(shell, SWT.PUSH | SWT.CENTER);
        props.setLook(wbToLoadFile);
        wbToLoadFile.setText( BaseMessages.getString( PKG, "ForexPrediction.Browse" ) );
        FormData fdbToLoadFile = new FormData();
        fdbToLoadFile.top = new FormAttachment( wToSave, margin );
        fdbToLoadFile.right = new FormAttachment( 100, 0 );
        wbToLoadFile.setLayoutData(fdbToLoadFile);

        wToLoadFile = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( wToLoadFile );
        wToLoadFile.addModifyListener( lsMod );
        FormData fdToLoadFile = new FormData();
        fdToLoadFile.left = new FormAttachment( middle, 0 );
        fdToLoadFile.right = new FormAttachment(wbToLoadFile, -margin );
        fdToLoadFile.top = new FormAttachment( wToSave, margin );
        wToLoadFile.setLayoutData(fdToLoadFile);
        wToLoadFile.setToolTipText( BaseMessages.getString( PKG, "ForexPrediction.ToLoadFile.Tooltip" ) );

        wbToLoadFile.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                DirectoryDialog dialog=new DirectoryDialog(shell,SWT.OPEN);
                if (wToLoadFile.getText() != null) {
                    dialog.setFilterPath(jobMeta.environmentSubstitute(wToLoadFile.getText()));
                }
                String dir = dialog.open();
                if (dir != null) {
                    wToLoadFile.setText(dir);
                }
            }
        } );


        Label wlConfigFile = new Label(shell, SWT.RIGHT);
        wlConfigFile.setText( BaseMessages.getString( PKG, "ForexPrediction.ConfigFile.Label" ) );
        props.setLook(wlConfigFile);
        FormData fdlConfigFile = new FormData();
        fdlConfigFile.left = new FormAttachment( 0, 0 );
        fdlConfigFile.top = new FormAttachment( wToLoadFile, margin );
        fdlConfigFile.right = new FormAttachment( middle, 0 );
        wlConfigFile.setLayoutData(fdlConfigFile);

        Button wbConfigFile = new Button(shell, SWT.PUSH | SWT.CENTER);
        props.setLook(wbConfigFile);
        wbConfigFile.setText( BaseMessages.getString( PKG, "ForexPrediction.Browse" ) );
        FormData fdbConfigFile = new FormData();
        fdbConfigFile.top = new FormAttachment( wToLoadFile, margin );
        fdbConfigFile.right = new FormAttachment( 100, 0 );
        wbConfigFile.setLayoutData(fdbConfigFile);

        wConfigFile = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook( wConfigFile );
        wConfigFile.addModifyListener( lsMod );
        FormData fdConfigFile = new FormData();
        fdConfigFile.left = new FormAttachment( middle, 0 );
        fdConfigFile.right = new FormAttachment(wbConfigFile, -margin );
        fdConfigFile.top = new FormAttachment( wToLoadFile, margin );
        wConfigFile.setLayoutData(fdConfigFile);
        wConfigFile.setToolTipText( BaseMessages.getString( PKG, "ForexPrediction.ConfigFile.Tooltip" ) );
        wbConfigFile.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                FileDialog dialog = new FileDialog( shell, SWT.SAVE );
                dialog.setFilterExtensions( new String[] { "*" } );
                if (wConfigFile.getText() != null) {
                    dialog.setFilterPath(jobMeta.environmentSubstitute(wToLoadFile.getText()));
                }
                dialog.setFilterNames( FILETYPES );
                if ( dialog.open() != null ) {
                    wConfigFile.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName());
                }
            }
        } );

        Button wOK = new Button(shell, SWT.PUSH);
        wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
        Button wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
        BaseStepDialog.positionBottomButtons( wConfigFile, new Button[] {wOK, wCancel}, margin, null );

        Listener lsCancel = new Listener() {
            public void handleEvent(Event e) {
                cancel();
            }
        };

        Listener lsOK = new Listener() {
            public void handleEvent(Event e) {
                ok();
            }
        };

        wCancel.addListener( SWT.Selection, lsCancel);
        wOK.addListener( SWT.Selection, lsOK);

        // Default listener when hitting enter
        SelectionAdapter lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };

        wName.addSelectionListener(lsDef);
        wFilename.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
        shell.addShellListener( new ShellAdapter() {
            public void shellClosed( ShellEvent e ) {
                cancel();
            }
        } );

        populateDialog();

        BaseStepDialog.setSize( shell );

        shell.open();
        while ( !shell.isDisposed() ) {
            if ( !display.readAndDispatch() ) {
                display.sleep();
            }
        }

        return meta;
    }

    private void dispose() {
        WindowProperty winprop = new WindowProperty( shell );
        props.setScreen( winprop );
        shell.dispose();
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void populateDialog() {
        wName.setText(Const.nullToEmpty( meta.getName()) );
        wFilename.setText(Const.nullToEmpty( meta.getFilename()) );
        wOutputFolder.setText(Const.nullToEmpty( meta.getOutput()) );
        wForecastSteps.setText(Const.nullToEmpty( meta.getForecastSteps()) );
        wToSave.setSelection( meta.isToSave() );
        wToLoadFile.setText(Const.nullToEmpty( meta.getToLoadFile()));
        wConfigFile.setText(Const.nullToEmpty(meta.getConfigFeaturesFile()));

        wName.selectAll();
        wName.setFocus();
    }

    private void cancel() {
        meta.setChanged( changed );
        meta = null;
        dispose();
    }

    /**
     * This method is called once the dialog is confirmed. It may only close the window if the
     * job entry has a non-empty name.
     */
    private void ok() {
        if (Utils.isEmpty(wName.getText()) ) {
            MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
            mb.setText( BaseMessages.getString( PKG, "System.StepJobEntryNameMissing.Title" ) );
            mb.setMessage( BaseMessages.getString( PKG, "System.JobEntryNameMissing.Msg" ) );
            mb.open();
            return;
        }

        meta.setName( wName.getText() );
        meta.setFilename( wFilename.getText() );
        meta.setOutput( wOutputFolder.getText() );
        meta.setForecastSteps( wForecastSteps.getText() );
        meta.setToSave( wToSave.getSelection() );
        meta.setToLoadFile( wToLoadFile.getText() );
        meta.setConfigFeaturesFile(wConfigFile.getText());

        dispose();
    }
}
