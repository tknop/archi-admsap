/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import com.archimatetool.editor.model.IModelExporter;
import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateComponent;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IFolder;



/**
 * Example Exporter of Archimate model
 * 
 * @author PricewaterhouseCoopers
 */
public class MyExporter implements IModelExporter {
    
    String MY_EXTENSION = ".rdf"; //$NON-NLS-1$
    String MY_EXTENSION_WILDCARD = "*.rdf"; //$NON-NLS-1$
    
    private OutputStreamWriter writer;
    
    public MyExporter() {
    }

    @Override
    public void export(IArchimateModel model) throws IOException {
        File file = askSaveFile();
        if(file == null) {
            return;
        }
        
        writer = new OutputStreamWriter(new FileOutputStream(file));
        
        writeFolder(model.getFolder(FolderType.BUSINESS));
        writeFolder(model.getFolder(FolderType.APPLICATION));
        writeFolder(model.getFolder(FolderType.TECHNOLOGY));
        writeFolder(model.getFolder(FolderType.CONNECTORS));
        writeFolder(model.getFolder(FolderType.RELATIONS));
        
        writer.close();
    }
    
    private void writeFolder(IFolder folder) throws IOException {
        List<EObject> list = new ArrayList<EObject>();
        
        getElements(folder, list);
        
        for(EObject eObject : list) {
            if(eObject instanceof IArchimateComponent) {
                IArchimateComponent component = (IArchimateComponent)eObject;
                String string = normalise(component.eClass().getName()) +
                        "," + normalise(component.getName()) //$NON-NLS-1$
                        + "," + normalise(component.getDocumentation()); //$NON-NLS-1$
                writer.write(string + "\n"); //$NON-NLS-1$
            }
        }
    }
    
    private void getElements(IFolder folder, List<EObject> list) {
        for(EObject object : folder.getElements()) {
            list.add(object);
        }
        
        for(IFolder f : folder.getFolders()) {
            getElements(f, list);
        }
    }

    private String normalise(String s) {
        if(s == null) {
            return ""; //$NON-NLS-1$
        }
        
        s = s.replace("\r\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
        s = "\"" + s + "\""; //$NON-NLS-1$ //$NON-NLS-2$
        
        return s;
    }

    /**
     * Ask user for file name to save to
     */
    private File askSaveFile() {
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        dialog.setText(Messages.MyExporter_0);
        dialog.setFilterExtensions(new String[] { MY_EXTENSION_WILDCARD, "*.*" } ); //$NON-NLS-1$
        String path = dialog.open();
        if(path == null) {
            return null;
        }
        
        // Only Windows adds the extension by default
        if(dialog.getFilterIndex() == 0 && !path.endsWith(MY_EXTENSION)) {
            path += MY_EXTENSION;
        }
        
        File file = new File(path);
        
        // Make sure the file does not already exist
        if(file.exists()) {
            boolean result = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),
                    Messages.MyExporter_0,
                    NLS.bind(Messages.MyExporter_1, file));
            if(!result) {
                return null;
            }
        }
        
        return file;
    }
}
