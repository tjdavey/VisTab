/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.jcu.bus.it.tjd.vistab;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.filechooser.FileFilter;


/**
 *
 * @author Twistie
 */
public abstract class VisExport {
    
    public VisExport(OWLModel model, OWLIndividual visualisationIndividual) throws invalidVisualisationInstanceException {
        
    };
    
    public abstract ArrayList<HashMap> getExportFileOutput(String filename) throws Exception;

    public abstract ArrayList<String> getExportFileList(String filename);
    
    public abstract class VisExportFilter extends FileFilter {
        
    }
    
    public abstract FileFilter getExportFilter();
    
    
}
class invalidVisualisationInstanceException extends Exception {

    public invalidVisualisationInstanceException() {
       super("Invalid instance for export.");
    }
}
