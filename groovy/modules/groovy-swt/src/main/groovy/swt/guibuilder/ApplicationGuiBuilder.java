/*
 * Created on Mar 1, 2004
 *
 */
package groovy.swt.guibuilder;

import groovy.jface.JFaceBuilder;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.util.GroovyScriptEngine;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * WORK IN PROGRESS, don't use this one yet
 * 
 * This will be the main guibuilder
 * 
 * TODO provide entry point for pull tools (e.g. i18n, security) TODO check
 * rebuild command
 * 
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class ApplicationGuiBuilder extends JFaceBuilder {
    private Logger log = Logger.getLogger(getClass().getName());

    /** the caching script engine */
    private GroovyScriptEngine scriptEngine;

    /**
     * tha constructor
     * 
     * @param rootUrl
     * @throws IOException
     */
    public ApplicationGuiBuilder(String rootUrl) throws IOException {
        scriptEngine = new GroovyScriptEngine(rootUrl);
    }

    /*
     * @see groovy.util.BuilderSupport#createNode(java.lang.Object,
     *      java.util.Map)
     */
    protected Object createNode(Object name, Map attributes) {
        Object widget = null;
        Composite parent = (Composite) attributes.remove("parent");
        String script = (String) attributes.remove("script");
        if ("run".equals(name) && parent != null && script != null) {
            if (parent instanceof Composite) {
                widget = runScript(script, parent);
            }
            else {
                log.log(Level.WARNING, "invalid parent: " + parent);
            }
        }
        else if ("rebuild".equals(name) && parent != null && script == null) {
            Closure closure = (Closure) attributes.remove("closure");
            if (closure != null) {
                disposeChildren(parent);
                closure.call(parent);
                parent.pack();
                parent.redraw();
            }
        }
        else {
            widget = super.createNode(name, attributes);
        }
        return widget;
    }

    /**
     * @param widget
     * @param script
     * @param parent
     * @return
     */
    private Object runScript(String script, Composite parent) {
        disposeChildren(parent);

        // build new widget
        Object widget = null;
        Binding binding = new Binding();
        binding.setVariable("parent", parent);
        binding.setVariable("guiBuilder", this);
        try {
            widget = scriptEngine.run(script, binding);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * dispose all children
     * 
     * @param parent
     */
    private void disposeChildren(Composite parent) {
        Control[] children = parent.getChildren();
        for (int i = 0; i < children.length; i++) {
            Control control = children[i];
            control.dispose();
        }
    }

    /*
     * @see groovy.util.BuilderSupport#createNode(java.lang.Object,
     *      java.lang.Object)
     */
    protected Object createNode(Object name, Object parent) {
        System.out.println("createNode: " + name + " parent: " + parent);
        return super.createNode(name, parent);
    }

    /*
     * @see groovy.util.BuilderSupport#createNode(java.lang.Object)
     */
    protected Object createNode(Object name) {
        System.out.println("createNode: " + name);
        return super.createNode(name);
    }
}
