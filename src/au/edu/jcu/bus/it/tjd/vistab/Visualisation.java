/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.jcu.bus.it.tjd.vistab;

/**
 *
 * @author Tristan Davey
 **/
 
public class Visualisation {
           
        //Classes
        public static final String VISUALISATION_CLASS = "OntologyVis:Visualisation";
        public static final String COMPONENT_CLASS = "";
        
        //Properties
        public static final String EXPORT_CLASS_PROPERTY = "OntologyVis:hasExportClass";
    
	private static Visualisation visualisation;
	/** A private Constructor prevents any other class from instantiating. */
	private Visualisation() {
		//	 Optional Code
	}
	public static synchronized Visualisation getInstance() {
		if (visualisation == null) {
			visualisation = new Visualisation();
		}
		return visualisation;
	}
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
