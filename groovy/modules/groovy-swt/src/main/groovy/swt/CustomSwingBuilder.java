/*
 * Created on Dec 15, 2004
 *
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a> 
 * $Id$
 */
package groovy.swt;

import groovy.swing.SwingBuilder;

/**
 * @author ckl
 *
 */
public class CustomSwingBuilder extends SwingBuilder {

    /* 
     * override to make public
     * 
     * @see groovy.util.BuilderSupport#getCurrent()
     */
    public Object getCurrent() {
        return super.getCurrent();
    }

    /*
     * override to make public
     *  
     * @see groovy.util.BuilderSupport#setCurrent(java.lang.Object)
     */
    public void setCurrent(Object current) {
        super.setCurrent(current);
    }
    
}