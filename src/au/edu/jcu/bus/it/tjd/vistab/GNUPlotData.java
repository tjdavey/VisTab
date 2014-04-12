package au.edu.jcu.bus.it.tjd.vistab;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class provides a method to simplify the process of building export files
 * for GNUPlot, by creating a model of the file output from values inserted with
 * keys. Built for the VisualisationTab Protege-OWL Extension.
 *  
 * @author      Tristan Davey
 * @version     %I%, %G%
 * @since       1.0
 */
public class GNUPlotData {
    
    // This will store the data for output 
    TreeMap<String, HashMap> data;
    ArrayList<String> columns;
    
    public GNUPlotData() {
        // Initialise the class elements
        this.data = new TreeMap<String, HashMap>(new DataComparator());
        this.columns = new ArrayList();
    }
    
    /**
     * Add a value to the file output model using the given index.
     * 
     * The advised format for column keys where multiple datasets share a common
     * key is "[dataset_identifier]_[variable_identifier]". If all datasets are
     * to be integrated without a shared index, the advised format is
     * "[variable_identifier]". It guarantees all inserts regardless of sanity 
     * to the point of producing mangled output. 
     * 
     * @todo                    Make searching for insertion indexes more 
     *                          efficient. Worst case Insertion time is currently
     *                          O(N) for each run of this function.
     *
     * @param columnKey         The String representation of the column key which
     *                          the dataValue value will be inserted into. See the
     *                          function description for advised formats for column
     *                          keys.
     * @param indexColumnKey    The String representation of the index column key
     *                          which will be used to insert the dataValue in the
     *                          row with a key matching indexValue in the column
     *                          with the column key matching this value. See the
     *                          function description for advised formats for column
     *                          keys.
     * @param indexValue        The String representation of the index value
     *                          which will be used to insert the dataValue in the
     *                          row with a key matching this in the column with 
     *                          the column key matching the indexColumnKey value.
     *                          See the function description for advised formats
     *                          for column keys.
     * @param dataValue         The actual value to be inserted.
     * @since     1.0
     */
    public void addDataPoint(String columnKey, String indexColumnKey, String indexValue, String dataValue) {        
        // Setup a boolean to determine if the value is added to existing row
        Boolean valueAdded = false;
        Iterator dataIt = this.data.values().iterator();
        // We must search each existing row, checking for the indexColumnKey
        
        while (dataIt.hasNext()) {
           HashMap row = (HashMap) dataIt.next();
           if(row.containsKey(indexColumnKey)) {
               // Check if the key passed in matches the one in this row
               if(row.get(indexColumnKey).equals(indexValue)) {
                   // Add run the column index update and add the value
                   listColumn(columnKey);
                   row.put(columnKey, dataValue);
                   // Set the boolean to false, so we can skip a new row add
                   valueAdded = true; 
               }
           }
        }
        
        //If the value wasn't added to an existing key we need to add a new row
        if (valueAdded == false) {
            //Create a new row Map for this row
            HashMap row = new HashMap();
            //Ensure this column and the index column are listed in the columns index
            listColumn(columnKey);
            listColumn(indexColumnKey);
            //Add datavalue to the row column Map
            row.put(columnKey, dataValue);
            row.put(indexColumnKey, indexValue);
            
            //Add the row map to the overall data model
            this.data.put(indexValue, row);
        }
    }
    
    /**
     * Returns a string of the file output for this instance of the class
     * 
     * @return      File output string.
     * 
     * @since       1.0
     */
    public String getDataFileOutput() {
        StringBuilder outputString = new StringBuilder();
        Iterator dataIt = this.data.values().iterator();
        while(dataIt.hasNext()) {
            HashMap row = (HashMap) dataIt.next();
            for(String colKey: this.columns) {
                if (row.containsKey(colKey)) {
                    String value = (String) row.get(colKey);
                    outputString.append(value+"\t");
                } else {
                    outputString.append("-\t");
                }
            }
            outputString.append("\n");
        }
        return outputString.toString();
    }

    /**
     * Ensures a valid list of all columns are kept. Call this function when
     * inserting any values with the column key to ensure it is listed.
     * 
     * @param columnKey     The key of the column for a value being inserted.
     * 
     * @since       1.0
     */
    private void listColumn(String columnKey) {
        if(!(this.columns.contains(columnKey))){
            this.columns.add(columnKey);
        }
    }
    
    /**
     * Returns an array containing an ordered list of the column keys. 
     * 
     * @return      ordered ArrayList of column keys.
     * 
     * @since       1.0
     */
    public ArrayList<String> getColumnOrder() {
        return this.columns;
    }
    
    
    //Tree Map Objects
    
    public static class DataComparator implements Comparator<String>
    {
        @Override
        public int compare (String c1, String c2) {
            return c1.compareTo(c2);
        }
    }


    
}


