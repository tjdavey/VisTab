/*
 * Generates a basic GNUPlot Script
 */
package au.edu.jcu.bus.it.tjd.vistab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tristan Davey
 */
public class GNUPlotScript {
    
    private static final String NL = "\n";
    
    public static final Character X_AXIS = 'x';
    public static final Character Y_AXIS = 'y';
    public static final Character Z_AXIS = 'z';
    
    
    private String initialUnset;
    private String title;
    private String labels;
    private String scales;
    private String time;
    private String plotInstructions;
    
    private String filename;
    
    
    public GNUPlotScript() {
        this.initialUnset = "";
        this.setInitialUnset(true, true);
        this.scales = "";
        this.title = "";
        this.labels = "";
        this.time = "";
        this.plotInstructions = "";
    }
    
    public void setInitialUnset(Boolean labelUnset, Boolean logUnset){
        if(labelUnset){
            this.initialUnset.concat("unset label"+NL);
        }
        if(logUnset){
            this.initialUnset.concat("unset log"+NL);
        }
    }
    
    public void setTime(Character axis) {
        Character axisChar = null;
        StringBuilder timeString = new StringBuilder();
        
        switch(axis) {
            default:
            case 'x':
                axisChar = 'x';
                timeString.append("set xdata time");
                break;
            case 'y':
                axisChar = 'y';
                timeString.append("set ydata time");
                break;
            case 'z':
                axisChar = 'z';
                timeString.append("set zdata time");
                break;
        }
        timeString.append(NL);
        timeString.append("set timefmt \"%Y-%m-%dT%H:%M:%S\"");
        timeString.append(NL);
        timeString.append("set format ").append(axisChar).append(" \"%Y-%m-%d %H:%M\"");
        this.time = timeString.toString();
    }
    
    public void setAutoScale(){
        this.scales = "set autoscale";
    }
    
    public void setScaleRange(float xRangeMin, float xRangeMax, float yRangeMin, float yRangeMax){
        this.scales.concat("set xrange ["+String.valueOf(xRangeMin)+":"+String.valueOf(xRangeMax)+"]"+NL);
        this.scales.concat("set yrange ["+String.valueOf(yRangeMin)+":"+String.valueOf(yRangeMax)+"]");
    }
    
    public void setScaleRange(float xRangeMin, float xRangeMax, float yRangeMin, float yRangeMax, float zRangeMin, float zRangeMax){
        this.scales.concat("set xrange ["+String.valueOf(xRangeMin)+":"+String.valueOf(xRangeMax)+"]"+NL);
        this.scales.concat("set yrange ["+String.valueOf(yRangeMin)+":"+String.valueOf(yRangeMax)+"]"+NL);
        this.scales.concat("set zrange ["+String.valueOf(zRangeMin)+":"+String.valueOf(zRangeMax)+"]");
    }
    
    public void setLabels(String xLabel, String yLabel){
        StringBuilder labelString = new StringBuilder();
        labelString.append("set xlabel \"").append(xLabel).append("\"");
        labelString.append(NL);
        labelString.append("set ylabel \"").append(yLabel).append("\"");
        this.labels = labelString.toString();
    }
    
    public void setLabels(String xLabel, String yLabel, String zLabel){
        StringBuilder labelString = new StringBuilder();
        labelString.append("set xlabel \"").append(xLabel).append("\"");
        labelString.append(NL);
        labelString.append("set ylabel \"").append(yLabel).append("\"");
        labelString.append(NL);
        labelString.append("set zlabel \"").append(zLabel).append("\"");
        this.labels = labelString.toString();
    }
    
    public void setTitle(String title){
        this.title = "set title \""+title+"\"";
    }
    
    public void buildPlot(ArrayList<HashMap> plotColumns, int dimensions){
        StringBuilder plotScript = new StringBuilder();
        if(dimensions > 2) {
            plotScript.append("splot ");
        } else {
            plotScript.append("plot ");
        }
        for(HashMap<String, ArrayList> dataSet: plotColumns) {
            
            ArrayList<String> dataSetPermutations = new ArrayList();
            
            //Create Permutations
            ArrayList<HashMap> xSet = dataSet.get("xColumnKeys");
            ArrayList<HashMap> ySet = dataSet.get("yColumnKeys");
            ArrayList<HashMap> zSet = dataSet.get("zColumnKeys");
            
            for(HashMap<String, String> xCol: xSet) {
                for(HashMap<String, String> yCol: ySet) {
                    if(dimensions == 2) {
                        dataSetPermutations.add(xCol.get("position")+":"+yCol.get("position"));
                    } else {
                        for(HashMap<String, String> zCol: zSet) {
                            dataSetPermutations.add(xCol.get("position")+":"+yCol.get("position")+":"+zCol.get("position"));
                        }
                    }
                }
            }
            for(String colOrder: dataSetPermutations) {
                plotScript.append("'").append(this.filename).append(".dat' using ").append(colOrder).append(" title '").append(dataSet.get("label")).append("' with ").append(dataSet.get("style")).append(", ");
            }
        }
        
        int lastCommaLocation = plotScript.lastIndexOf(", ");
        StringBuilder plotScriptFinal = new StringBuilder(plotScript.substring(0, lastCommaLocation));
        plotScriptFinal.append(";");
        this.plotInstructions = plotScriptFinal.toString();
    }
    
    public void setFilename(String dataFilename){
        this.filename = dataFilename;
    }
    
    public String getScript(){
        StringBuilder script = new StringBuilder();

        
        script.append("set datafile missing \"-\"");
        script.append(GNUPlotScript.NL);
        script.append(this.title);
        script.append(GNUPlotScript.NL);
        script.append(this.labels);
        script.append(GNUPlotScript.NL);
        script.append(this.time);
        script.append(GNUPlotScript.NL);
        script.append(this.scales);
        script.append(GNUPlotScript.NL);
        script.append(this.plotInstructions);
        
        return script.toString();
    }
}
