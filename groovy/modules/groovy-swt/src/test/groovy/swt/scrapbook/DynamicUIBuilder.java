/*
 * Created on Mar 7, 2004
 *
 */
package groovy.swt.scrapbook;

import groovy.jface.JFaceBuilder;

import java.util.Map;

/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster</a> 
 * $Id$
 */
public class DynamicUIBuilder extends JFaceBuilder {
    public DynamicUIBuilder() {
        super();
    }

    /*
     * @see groovy.util.BuilderSupport#createNode(java.lang.Object)
     */
    public Object createNode(Object name) {
//        System.out.println("createNode: " + name);
        return super.createNode(name);
    }

    /*
     * @see groovy.util.BuilderSupport#getCurrent()
     */
    protected Object getCurrent() {
        return super.getCurrent();
    }
    /* 
     * @see groovy.util.BuilderSupport#createNode(java.lang.Object, java.util.Map)
     */
    protected Object createNode(Object name, Map attributes) {
        System.out.println("createNode: " + name + " [attributes:]  " + attributes);
        return super.createNode(name, attributes);
    }

    /* 
     * @see groovy.util.BuilderSupport#createNode(java.lang.Object, java.lang.Object)
     */
    protected Object createNode(Object name, Object parent) {
        System.out.println("createNode: " + name + " [parent:] " + parent);
        return super.createNode(name, parent);
    }

}
