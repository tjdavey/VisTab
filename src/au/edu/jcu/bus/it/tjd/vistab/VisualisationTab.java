package au.edu.jcu.bus.it.tjd.vistab;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protegex.owl.ui.*;
import edu.stanford.smi.protegex.owl.ui.cls.*;
import edu.stanford.smi.protege.widget.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protegex.owl.inference.reasoner.ProtegeReasoner;
import edu.stanford.smi.protegex.owl.model.event.ClassAdapter;
import edu.stanford.smi.protegex.owl.ui.individuals.AssertedInstancesListPanel;
import edu.stanford.smi.protegex.owl.ui.individuals.IndividualsTabClassesPanel;
import edu.stanford.smi.protegex.owl.ui.individuals.InstancesList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;

// an example tab
public class VisualisationTab extends AbstractTabWidget {
    
    private OWLModel model;
    private VisExport exportInstance; 
    
    private VisualisationInstancesListPanel visIndPanel;
    private JPanel customPanel;
    private JPanel exportPanel;
    private JPanel classPanel;
    private JPanel exportSelectPanel;
    private JPanel exportSummaryPanel;
    private JPanel commandPanel;
    
    private JTextField fileField;
    private JButton fileButton;
    private JFileChooser fileChooser;
    
    private JTextArea summaryText;
    private JScrollPane summaryScroll;
    
    private JButton exportButton; 
    
    
    
    private JPanel infoPanel;
    private JTextArea log;
    private JScrollPane logScroll;
    
    private JTextField classText;
    
    private VisExport exporter;
    

    // startup code
    public void initialize() {
        
        this.model = (OWLModel) getKnowledgeBase();
        //this.reasoner = new createPelletOWLAPIReasoner(model);
        
        JSplitPane rightPane, splitter;
        JComponent indViewPanel;
        
        Border classBorder = BorderFactory.createTitledBorder("Export Method");
        Border exportSelectBorder = BorderFactory.createTitledBorder("File Export Location");
        Border exportSummaryBorder = BorderFactory.createTitledBorder("Export Action Summary");
        
        JLabel classTextLabel = new JLabel("Export Class:");
        JLabel summaryLabel = new JLabel("The following files will be written/overwritten:");
        
        this.infoPanel = new JPanel();
        this.exportPanel = new JPanel();
        this.classPanel = new JPanel();
        this.exportSelectPanel = new JPanel();
        this.exportSummaryPanel = new JPanel();
        this.commandPanel = new JPanel();
        
        this.fileField = new JTextField();
        //this.fileField.setEnabled(false);
        this.fileField.setColumns(50);
        this.fileButton = new JButton("Browse");
        //this.fileButton.setEnabled(false);
        this.fileChooser = new JFileChooser();
        
        this.summaryText = new JTextArea();
        this.summaryText.setEditable(false);
        this.summaryScroll = new JScrollPane(this.summaryText);
        
        this.exportButton = new JButton("Export");
        this.exportButton.setEnabled(false);
        
        log = new JTextArea();
        log.setRows(10);
        logScroll = new JScrollPane(log);
        classText = new JTextField();
        classText.setColumns(100);
        
        //Configure Export Class Panel
        classPanel.setBorder(classBorder);
        classPanel.add(classTextLabel);
        classPanel.add(classText);
        
        //Configure Export Selection Panel;
        this.exportSelectPanel.setBorder(exportSelectBorder);
        this.setLayout(new FlowLayout());
        this.exportSelectPanel.add(this.fileField);
        this.exportSelectPanel.add(this.fileButton);
        
        //Configure Summary Panel
        this.exportSummaryPanel.setBorder(exportSummaryBorder);
        this.exportSummaryPanel.setLayout(new BorderLayout());
        this.exportSummaryPanel.add(summaryLabel, BorderLayout.NORTH);
        this.exportSummaryPanel.add(this.summaryScroll, BorderLayout.CENTER);
        
        //Configure Export Command Panel
        commandPanel.setLayout(new FlowLayout());
        commandPanel.add(exportButton);
        commandPanel.setMinimumSize(new Dimension(200, 50));
        
        //Configure Logger
        log.setLineWrap(false);
        log.setEditable(false);
        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScroll.setWheelScrollingEnabled(true);
        logScroll.setMinimumSize(new Dimension(300, 200));
        
        //Configure Export Panel
        exportPanel.setLayout(new GridLayout(0, 2));
        exportPanel.add(this.exportSelectPanel, BorderLayout.WEST);
        exportPanel.add(this.exportSummaryPanel, BorderLayout.EAST);
        
        
        //Add Elements to export Panel
        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(logScroll);
        
        
                
        // initialize the tab label
        setLabel("Visualisation");
        setIcon(Icons.getQueryExportIcon());
        
        //Create Custom Panel 
        JPanel instancePanel = new JPanel();
        JPanel centerPanel = new JPanel();
        instancePanel.setLayout(new BorderLayout());
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(this.exportPanel, BorderLayout.CENTER);
        centerPanel.add(this.commandPanel, BorderLayout.SOUTH);
        instancePanel.add(centerPanel, BorderLayout.CENTER);
        instancePanel.add(this.classPanel, BorderLayout.NORTH);
        instancePanel.add(this.infoPanel, BorderLayout.SOUTH);
        
        //Create Individual Selection Panel
        indViewPanel = createIndWidget();
        
        //Construct Layout
        setLayout(new BorderLayout());
        splitter = ComponentFactory.createLeftRightSplitPane();
        add(splitter, BorderLayout.CENTER);
        splitter.setLeftComponent(indViewPanel);
        splitter.setRightComponent(instancePanel);
        
        //Setup Listeners
        fileButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Integer status = fileChooser.showSaveDialog(infoPanel);
                addLog("File Selected. Status: ".concat(status.toString()));
                if (status == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String filePath = selectedFile.getAbsolutePath();
                    //filePath.substring(0, filePath.lastIndexOf("."));
                    filePath.concat("*");
                    fileField.setText(filePath);
                    fileSummaryUpdate();
                    disablePanelChildren(commandPanel, false);
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });
        
        exportButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                    fileExport();
                }
            @Override
            public void mousePressed(MouseEvent e) {
            }
            @Override
            public void mouseReleased(MouseEvent e) {
            }
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        
    }
    
    private void addLog(String logString) {
        StringBuilder build = new StringBuilder(logString);
        int endPosition = log.getText().length();
        build.insert(0, '\n');
        log.insert(build.toString(), endPosition);
        try {
            Rectangle bottom = log.modelToView(endPosition);
            logScroll.scrollRectToVisible(bottom);
        } catch (BadLocationException ex) {
            Logger.getLogger(VisualisationTab.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void fileSummaryUpdate() {
        String filePath = this.fileField.getText();
        String filename = filePath.substring(filePath.lastIndexOf("\\"), filePath.length());
        ArrayList<String> exportList = this.exportInstance.getExportFileList(filename);
        for(String file: exportList) {
            this.summaryText.insert(file.concat("\n"), this.summaryText.getText().length());
        }
    }
    
    private void fileExport() {
        String filePath = this.fileField.getText();
        String filename = filePath.substring((filePath.lastIndexOf("\\")+1), filePath.length());
        String fileDirectory = filePath.substring(0, (filePath.lastIndexOf("\\")+1));
        try {
            ArrayList<HashMap> exportList = exportInstance.getExportFileOutput(filename);
            for(HashMap exportFile: exportList) {
                addLog("Writing File: ".concat(fileDirectory.concat((String) exportFile.get("filename"))));
                FileWriter fStream;
                fStream = new FileWriter(fileDirectory.concat((String) exportFile.get("filename")));
                BufferedWriter fOut = new BufferedWriter(fStream);
                fOut.write((String) exportFile.get("output"));
                fOut.close();
                fStream.close();
                addLog("File Write Completed");
            }
        } catch (Exception ex) {
            addLog("Error: ".concat(ex.toString()));
            Logger.getLogger(VisualisationTab.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void disablePanelChildren(JPanel panel, boolean disabled) {
        Component[] compArray = panel.getComponents();
        for(Component comp: compArray) {
            comp.setEnabled(!disabled);
        }
    }
    
    private JComponent createIndWidget() {
        Cls classParent = model.getCls(Visualisation.VISUALISATION_CLASS);
        Collection<Cls> classCollection = new HashSet<Cls>();
        classCollection.add(classParent);
        
        visIndPanel = new VisualisationInstancesListPanel(model);
        visIndPanel.setClses(classCollection);

        
        //Listen for Selection of Individuals
        visIndPanel.addSelectionListener(new SelectionListener() {
            @Override
            public void selectionChanged(SelectionEvent event) {
                Collection selection = visIndPanel.getSelection();
                Instance selectedInstance;
                if (selection.size() == 1) {
                    selectedInstance = (Instance) CollectionUtilities.getFirstItem(selection);
                }
                else {
                    selectedInstance = null;
                }
                if(selectedInstance == null || selectedInstance instanceof RDFResource) {
                    OWLIndividual resource = (OWLIndividual) selectedInstance;
                    setResource(resource);
                }
            }
        });
        
        //Listen for Model Changes which effect this tab
        /*classParent.addClsListener(new ClassAdapter() {
            public void instanceAdded(RDFSClass cls, RDFResource instance) {
                visIndPanel.reload();
                System.out.println("Update Detected");
            }
        });*/
        
        return (JComponent) visIndPanel;
    }
    
    private void setResource(OWLIndividual resource) {
        disablePanelChildren(exportPanel, true);
        disablePanelChildren(commandPanel, true);
        addLog("Loading Resource: ".concat(resource.getBrowserText()));
        OWLDatatypeProperty exportClassProperty = model.getOWLDatatypeProperty(Visualisation.EXPORT_CLASS_PROPERTY);
        OWLIndividual individual = (OWLIndividual) resource;
        
        String packageName = "au.edu.jcu.bus.it.tjd.vistab.";
        String value = (String) individual.getPropertyValue(exportClassProperty);
        
        if(value == null){
            classText.setText("");
            addLog("Error: hasExportClass Property not set.\n");
            return;
        }
        
        classText.setText(value);
        
        
        try {
            addLog("Loading Class: ".concat(value));
            Class exportClass = Class.forName(value);
            Constructor con = exportClass.getConstructor(OWLModel.class, OWLIndividual.class);
            this.exportInstance = (VisExport) con.newInstance(model, individual);     
            
        }
        catch (ClassNotFoundException ex) {
            addLog("Error: No Class found matching Visualisation Instance. Please ensure you have the correct export modules for VisualisationTab Installed.");
            Logger.getLogger(VisualisationTab.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        catch (NoSuchMethodException ex) {
            addLog("Error: Loaded class does not contain a valid constuctor for export.");
            Logger.getLogger(VisualisationTab.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        catch (InvocationTargetException ex) {
            addLog("Exception: ".concat(ex.toString()));
            addLog("Cause: ".concat(ex.getCause().toString()).concat(ex.getCause().getMessage()));
            addLog("Note: If the above exception message reads 'null slot' it generally indicates that an asserted property is not part of this instance.");
            Logger.getLogger(VisualisationTab.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        catch (Exception ex) {
            addLog("Exception: ".concat(ex.toString()));
            addLog("Error: ".concat(ex.getMessage()));
            Logger.getLogger(VisualisationTab.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        disablePanelChildren(exportPanel, false);
        if(this.fileField.getText().length() > 0) {
            disablePanelChildren(commandPanel, false);
        }
        fileChooser.setFileFilter(exportInstance.getExportFilter());
        
        
    }
    
    private JComponent createCustomWidget() {
        customPanel = new JPanel();
        
        JPanel currentSelection = new JPanel();
        TitledBorder currentSelectionTitle = BorderFactory.createTitledBorder("title");
        currentSelection.setBorder(currentSelectionTitle);
        
        JLabel selectedVisLabel = new JLabel("Selected Visualisation:");
        JLabel exportVisLabel = new JLabel("Export Format:");
        
        currentSelection.add(selectedVisLabel);
        currentSelection.add(exportVisLabel);
        
        return customPanel;
    }
    
    public static boolean isSuitable(Project project, Collection errors) {
        boolean isSuitable = true;
        if (project.getKnowledgeBase().getCls(Visualisation.VISUALISATION_CLASS) == null) {
            isSuitable = false;
            errors.add("This Tab can only be used with OWL projects containing the OntologyVis Ontologies.");
        }
        return isSuitable;
    }
    
    // this method is useful for debugging
    public static void main(String[] args) {
        edu.stanford.smi.protege.Application.main(args);
    }
}
