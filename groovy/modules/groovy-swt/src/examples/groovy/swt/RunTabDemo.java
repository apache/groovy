/*
 * Created on Apr 19, 2004
 *  
 */
package groovy.swt;

import groovy.lang.GroovyObject;

/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a> 
 * $Id$
 */
public class RunTabDemo {
    public static void main(String[] args) throws Exception {
        RunDemoBrowser demo = new RunDemoBrowser();
        GroovyObject object = demo.compile("src/examples/groovy/swt/TabDemo.groovy");
        object.invokeMethod("run", null);
    }

}