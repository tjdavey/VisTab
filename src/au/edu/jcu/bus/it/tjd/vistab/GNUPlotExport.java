/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.jcu.bus.it.tjd.vistab;

import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.RDFSDatatype;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Tristan Davey
 */
public class GNUPlotExport extends VisExport {

    public static final String GNUPLOT_CLASS = "OntologyVis:GNUPlot";
    public static final String VIS_PREFIX = "OntologyVis:";
    public static final String EXPORT_FORMAT = "GNUPlot Format (.dat, .gpl)";
    
    public static final String EXPORT_PROPERTY_DATASET = "hasDataSet";
    public static final String EXPORT_PROPERTY_DATAPOINTS = "hasDataPoint";
    public static final String EXPORT_PROPERTY_XAXIS = "hasXAxisProperty";
    public static final String EXPORT_PROPERTY_YAXIS = "hasYAxisProperty";
    public static final String EXPORT_PROPERTY_ZAXIS = "hasZAxisProperty";
    public static final String EXPORT_PROPERTY_XAXISLABEL = "hasXAxisLabel";
    public static final String EXPORT_PROPERTY_YAXISLABEL = "hasYAxisLabel";
    public static final String EXPORT_PROPERTY_ZAXISLABEL = "hasZAxisLabel";
    public static final String EXPORT_PROPERTY_INDEX = "hasIndexProperty";
    public static final String EXPORT_PROPERTY_LABEL = "hasLabel";
    public static final String EXPORT_PROPERTY_STYLE = "hasStyle";
    
    public static final String DATATYPE_DATETIME = "xsd:date";
    public static final String DATATYPE_STRING = "xsd:string";
    

    private OWLModel model;

    private OWLIndividual visIndividual;
    
    private Collection xAxisProperty;
    private Collection yAxisProperty;
    private Collection zAxisProperty;
    private OWLProperty indexProperty;
    private Character dateTimeAxis; 
    
    private ArrayList<HashMap> dataSetColumns;
    private ArrayList<String> columnOrder;
    
    private int dimensions; 
    
    public GNUPlotExport(OWLModel model, OWLIndividual visualisationIndividual) throws invalidVisualisationInstanceException{
            //Mandatory Super Call
            super(model, visualisationIndividual);
            
            //Initialise and Build Everything
            OWLObjectProperty xAxisOWLProperty = model.getOWLObjectProperty(VIS_PREFIX+EXPORT_PROPERTY_XAXIS);
            OWLObjectProperty yAxisOWLProperty = model.getOWLObjectProperty(VIS_PREFIX+EXPORT_PROPERTY_YAXIS);
            OWLObjectProperty zAxisOWLProperty = model.getOWLObjectProperty(VIS_PREFIX+EXPORT_PROPERTY_ZAXIS);
            OWLObjectProperty indexOWLProperty = model.getOWLObjectProperty(VIS_PREFIX+EXPORT_PROPERTY_INDEX);
            
            this.columnOrder = new ArrayList();
            this.dataSetColumns = new ArrayList();
            
            if(visualisationIndividual.hasType(model.getCls(GNUPLOT_CLASS))){
                this.model = model;
                
                this.visIndividual = visualisationIndividual;
            } else {
                throw new invalidVisualisationInstanceException();
            }
            
            this.xAxisProperty = this.visIndividual.getPropertyValues(xAxisOWLProperty);
            this.yAxisProperty = this.visIndividual.getPropertyValues(yAxisOWLProperty);
            //If a z axis property is set, we'll retrieve it.
            if(this.visIndividual.getPropertyValueCount(zAxisOWLProperty) == 1) {
                this.dimensions = 3;
                this.zAxisProperty = this.visIndividual.getPropertyValues(zAxisOWLProperty);
            } else {
                this.dimensions = 2;
                this.zAxisProperty = null;
            }
            //Check if an index is set, set field 
            if(this.visIndividual.getPropertyValueCount(indexOWLProperty) == 1) {
                this.indexProperty = (OWLProperty) this.visIndividual.getPropertyValue(indexOWLProperty);
            } else {
                this.indexProperty = null;
            }
            
            
    }
    
    private String buildScriptFile(String filename) throws Exception{
        /* The Data file MUST be processed before building the script as
         * values are required from the data file build process. This tests
         * one of those values and will throw an exception if not correct.
         */
        if(this.columnOrder != null ) {
            
            // Retrieve all the OWLProperties
            OWLDatatypeProperty titleProperty = model.getOWLDatatypeProperty(VIS_PREFIX+EXPORT_PROPERTY_LABEL);
            OWLDatatypeProperty xAxisLabelOWLProperty = model.getOWLDatatypeProperty(VIS_PREFIX+EXPORT_PROPERTY_XAXISLABEL);
            OWLDatatypeProperty yAxisLabelOWLProperty = model.getOWLDatatypeProperty(VIS_PREFIX+EXPORT_PROPERTY_YAXISLABEL);
            OWLDatatypeProperty zAxisLabelOWLProperty = model.getOWLDatatypeProperty(VIS_PREFIX+EXPORT_PROPERTY_ZAXISLABEL);
            
            String xLabel, yLabel, zLabel, title, style; 
            
            //Get Visualisation Variables
            if(this.visIndividual.hasPropertyValue(xAxisLabelOWLProperty)) {
                xLabel = (String) this.visIndividual.getPropertyValue(xAxisLabelOWLProperty);
            } else {
                xLabel = "";
            }
            
            if(this.visIndividual.hasPropertyValue(yAxisLabelOWLProperty)) {
                yLabel = (String) this.visIndividual.getPropertyValue(yAxisLabelOWLProperty);
            } else {
                yLabel = "";
            }
            
            if(this.zAxisProperty != null) {
                if(this.visIndividual.hasPropertyValue(zAxisLabelOWLProperty) && this.dimensions > 2) {
                    zLabel = (String) this.visIndividual.getPropertyValue(zAxisLabelOWLProperty);
                } else {
                    zLabel = "";
                }
            } else {
                zLabel = null;
            }
            

            
            //Build the object
            GNUPlotScript scriptObject = new GNUPlotScript();            
            
            //Get the title, if one is not provided, GNUPlot will fall back to default Plot method
            if(this.visIndividual.hasPropertyValue(titleProperty)) {
                scriptObject.setTitle((String) this.visIndividual.getPropertyValue(titleProperty));
            }
            if(this.dateTimeAxis != null) {
                scriptObject.setTime(this.dateTimeAxis);
            }
            scriptObject.setAutoScale();
            scriptObject.setFilename(filename);
            if(this.dimensions > 2) {
                scriptObject.setLabels(xLabel, yLabel, zLabel);
            } else {
                scriptObject.setLabels(xLabel, yLabel);
            }
            
            scriptObject.buildPlot(this.dataSetColumns, dimensions);
            
            return scriptObject.getScript();
        } else {
            throw new Exception("Method called before mandatory pre-requisate method (buildDataFile) called.");
        }
    }
    
    private String buildDataFile(){
        
        //Ensure data set columns are empty;
        this.dataSetColumns.clear();
        
        //Properties to retrieve
        OWLObjectProperty dataSetProperty = model.getOWLObjectProperty(VIS_PREFIX+EXPORT_PROPERTY_DATASET);
        OWLObjectProperty dataPointProperty = model.getOWLObjectProperty(VIS_PREFIX+EXPORT_PROPERTY_DATAPOINTS);
        OWLProperty labelProperty = model.getOWLProperty(VIS_PREFIX+EXPORT_PROPERTY_LABEL);
        OWLProperty styleProperty = model.getOWLProperty(VIS_PREFIX+EXPORT_PROPERTY_STYLE);
        
        RDFSDatatype xsdDateTime = model.getRDFSDatatypeByName(DATATYPE_DATETIME);
        RDFSDatatype xsdString = model.getRDFSDatatypeByName(DATATYPE_STRING);
                  
        Collection dataSetComponents = this.visIndividual.getPropertyValues(dataSetProperty);      
        Iterator dataSetIterator = dataSetComponents.iterator();
        
        GNUPlotData data = new GNUPlotData();
        
        //Create an Arraylist of column keys
        //Note this performs both error checking and collates the column names
        ArrayList<String> xAxisColumnKeys = new ArrayList();
        ArrayList<String> yAxisColumnKeys = new ArrayList();
        ArrayList<String> zAxisColumnKeys = new ArrayList();
        
        while(dataSetIterator.hasNext()) {
            OWLIndividual dataSetInd = (OWLIndividual) dataSetIterator.next();
            Collection dataPoints = dataSetInd.getPropertyValues(dataPointProperty);
            Iterator dataPointIterator = dataPoints.iterator();
            
            //Get the dataset label property
            String label = "";
            RDFSLiteral labelLiteral = dataSetInd.getPropertyValueLiteral(labelProperty);
            if(labelLiteral != null) {
                label = labelLiteral.getString();
            }
            //Get the dataset style property
            String style = "";
            RDFSLiteral styleLiteral = dataSetInd.getPropertyValueLiteral(styleProperty);
            if(styleLiteral != null) {
                style = styleLiteral.getString();
            } else {
                style = "line";
            }
            
            while(dataPointIterator.hasNext()) {
                //Declare Variables
                String indexValue;
                String indexKey;
                String columnPrefix;
                
                OWLIndividual dataPointInd = (OWLIndividual) dataPointIterator.next();
                //Check Index is set. If Index is not set, UID is used as an index
                if(this.indexProperty != null) {
                    RDFSLiteral indexValueLiteral = dataPointInd.getPropertyValueLiteral(this.indexProperty);
                    if(indexValueLiteral != null) {
                        indexValue = indexValueLiteral.getString();
                        indexKey = this.indexProperty.getName();
                        columnPrefix = dataSetInd.getName().concat("_");
                    } else {
                        indexValue = "-";
                        indexKey = this.indexProperty.getName();
                        columnPrefix = dataSetInd.getName().concat("_");
                    }
                } else {
                    indexValue = dataPointInd.getName();
                    indexKey = "UID";
                    columnPrefix = "";
                }
                
                //Retrieve the axis values and insert
                if(this.xAxisProperty != null) {
                    if(!this.xAxisProperty.isEmpty()) {
                        Iterator xI = this.xAxisProperty.iterator();
                        while(xI.hasNext()) {
                            OWLProperty x = (OWLProperty) xI.next();
                            RDFSDatatype test = x.getRangeDatatype();
                            if(x.getRangeDatatype().equals(xsdDateTime)) {
                                this.dateTimeAxis = GNUPlotScript.X_AXIS;
                            }
                            RDFSLiteral value = dataPointInd.getPropertyValueLiteral(x);
                            if(value != null) {
                                String xAxisValue = value.getString();
                                if(value.getDatatype().equals(xsdString)) {
                                    xAxisValue = "\""+xAxisValue+"\"";
                                }
                                String xColumnKey = columnPrefix.concat(x.getName());
                                data.addDataPoint(xColumnKey, indexKey, indexValue, xAxisValue);
                                this.uniqueAddColumnKey(xAxisColumnKeys, xColumnKey);
                            }
                        }
                    }
                }
                if(this.yAxisProperty != null) {
                    if(!this.yAxisProperty.isEmpty()) {
                        Iterator yI = this.yAxisProperty.iterator();
                        while(yI.hasNext()) {
                            OWLProperty y = (OWLProperty) yI.next();
                            if(y.getRangeDatatype().equals(xsdDateTime)) {
                                this.dateTimeAxis = GNUPlotScript.Y_AXIS;
                            }
                            RDFSLiteral value = dataPointInd.getPropertyValueLiteral(y);
                            if(value != null) {
                                String yAxisValue = value.getString();
                                if(value.getDatatype().equals(xsdString)) {
                                    yAxisValue = "\""+yAxisValue+"\"";
                                }
                                String yColumnKey = columnPrefix.concat(y.getName());
                                data.addDataPoint(yColumnKey, indexKey, indexValue, yAxisValue);
                                this.uniqueAddColumnKey(yAxisColumnKeys, yColumnKey);
                            }
                        }
                    }
                }
                if(this.zAxisProperty != null) {
                    if(!this.zAxisProperty.isEmpty()) {
                        Iterator zI = this.zAxisProperty.iterator();
                        while(zI.hasNext()) {
                            OWLProperty z = (OWLProperty) zI.next();
                            if(z.getRangeDatatype().equals(xsdDateTime)) {
                                this.dateTimeAxis = GNUPlotScript.Y_AXIS;
                            }
                            RDFSLiteral value = dataPointInd.getPropertyValueLiteral(z);
                            if(value != null) {
                                String zAxisValue = value.getString();
                                if(value.getDatatype().equals(xsdString)) {
                                    zAxisValue = "\""+zAxisValue+"\"";
                                }
                                String zColumnKey = columnPrefix.concat(z.getName());
                                data.addDataPoint(zColumnKey, indexKey, indexValue, zAxisValue);
                                this.uniqueAddColumnKey(zAxisColumnKeys, zColumnKey);
                            }
                        }
                    }
                }
            }
            
            this.columnOrder = data.getColumnOrder();

            //Add Dataset to Dataset Column Collection
            HashMap<String, Object> dataSetMap = new HashMap();
            if(!xAxisColumnKeys.isEmpty()) {
                ArrayList<HashMap> xList = new ArrayList();
                for(String colKey: xAxisColumnKeys) {
                    HashMap<String, String> colMap = new HashMap();
                    colMap.put("key", colKey);
                    Integer pos = this.columnOrder.indexOf(colKey)+1;
                    colMap.put("position", pos.toString());
                    xList.add(colMap);
                }
                dataSetMap.put("xColumnKeys", xList);
            }

            if(!yAxisColumnKeys.isEmpty()) {
                ArrayList<HashMap> yList = new ArrayList();
                for(String colKey: yAxisColumnKeys) {
                    HashMap<String, String> colMap = new HashMap();
                    colMap.put("key", colKey);
                    Integer pos = this.columnOrder.indexOf(colKey)+1;
                    colMap.put("position", pos.toString());
                    yList.add(colMap);
                }
                dataSetMap.put("yColumnKeys", yList);
            }

            if(!zAxisColumnKeys.isEmpty()) {
                ArrayList<HashMap> zList = new ArrayList();
                for(String colKey: zAxisColumnKeys) {
                    HashMap<String, String> colMap = new HashMap();
                    colMap.put("key", colKey);
                    Integer pos = this.columnOrder.indexOf(colKey)+1;
                    colMap.put("position", pos.toString());
                    zList.add(colMap);
                }
                dataSetMap.put("zColumnKeys", zList);
                

            }
            dataSetMap.put("label", label);
            dataSetMap.put("style", style);
            this.dataSetColumns.add(dataSetMap);
            xAxisColumnKeys.clear();
            yAxisColumnKeys.clear();
            zAxisColumnKeys.clear();
        }

        
        
       
        String output = data.getDataFileOutput();
        return output;
    }
    
    private void uniqueAddColumnKey(ArrayList<String> columnKeyList, String columnKey) {
        if(!(columnKeyList.contains(columnKey))) {
            columnKeyList.add(columnKey);
        }
    }
    
    @Override
    public ArrayList<HashMap> getExportFileOutput(String filename) throws Exception {
        ArrayList<HashMap> exportObject = new ArrayList();
        String data = this.buildDataFile();
        String script = this.buildScriptFile(filename);
        
        HashMap dataMap = new HashMap();
        HashMap scriptMap = new HashMap();
        
        dataMap.put("filename", filename.concat(".dat"));
        dataMap.put("output", data);
        
        scriptMap.put("filename", filename.concat(".gpl"));
        scriptMap.put("output", script);
                
        exportObject.add(dataMap);
        exportObject.add(scriptMap);
                
        return exportObject;        
    }
    
    @Override
    public FileFilter getExportFilter() {
        return new GNUPlotExportFilter();
    }

    @Override
    public ArrayList<String> getExportFileList(String filename) {
        ArrayList<String> exportFileList = new ArrayList();
        exportFileList.add(filename.concat(".dat"));
        exportFileList.add(filename.concat(".gpl"));
        return exportFileList;
    }
    
    private class GNUPlotExportFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            
            if(f.isDirectory()){
                return true;
            }
            
            String extension = getExtension(f);
            if(extension != null) {
                if(extension.equals("dat") || extension.equals("gpl")){
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
            
        }

        @Override
        public String getDescription() {
            return "GNUPlot Formats (.dat, .gpl)";
        }
        
        public String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');
            
            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }
            return ext;
        }
        
    }
    
}
