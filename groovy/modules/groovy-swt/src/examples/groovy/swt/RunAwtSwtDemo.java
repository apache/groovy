/*
 * Created on Apr 19, 2004
 *  
 */
package groovy.swt;

import groovy.lang.GroovyObject;

/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a> $Id$
 */
public class RunAwtSwtDemo {
    
    public static void main(String[] args) throws Exception {
        RunDemoBrowser demo = new RunDemoBrowser();
        GroovyObject object = demo.compile("src/examples/groovy/swt/AwtSwtDemo.groovy");
        object.invokeMethod("run", null);
    }
    
//    public static void main(String[] args) {
//        Display display = new Display();
//        Shell shell = new Shell(display);
//        Composite locationComp = new Composite(shell, SWT.DEFAULT);
//        shell.setLayout(new FillLayout());
//        
//        java.awt.Frame locationFrame = SWT_AWT.new_Frame(locationComp);
//        
//        locationFrame.add(new JTree());
//        
//        shell.open();
//        while (!shell.isDisposed()) {
//            if (!display.readAndDispatch()) display.sleep();
//        }
//        display.dispose();
//        
//    }
}