/*
 * Created on Mar 17, 2004
 *  
 */
package groovy.swt.guibuilder;

import groovy.lang.Binding;
import groovy.lang.MissingPropertyException;
import groovy.swt.SwtUtils;
import groovy.swt.factory.AbstractSwtFactory;
import groovy.swt.factory.SwtFactory;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.util.Map;
import java.util.logging.Logger;

import org.codehaus.groovy.GroovyException;
import org.eclipse.swt.widgets.Composite;

/**
 * Run another script
 * 
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a> 
 * $Id$
 */
public class RunScriptFactory extends AbstractSwtFactory implements SwtFactory {

    /** the logger */
    private Logger log = Logger.getLogger(getClass().getName());

    /** the builder */
    private ApplicationGuiBuilder guiBuilder;

    /**
     * @param scriptEngine
     */
    public RunScriptFactory(ApplicationGuiBuilder guiBuilder) {
        this.guiBuilder = guiBuilder;
    }

    /*
     * @see groovy.swt.factory.AbstractSwtFactory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent) throws GroovyException {

        // get src
        String src = (String) properties.remove("src");
        if (src == null) {
            throw new MissingPropertyException("src", RunScriptFactory.class);
        }

        // get binding
        Binding binding = (Binding) properties.remove("binding");
        if (binding == null) {
            binding = new Binding();
        }

        // get parent composite
        Composite parentComposite = null;
        Object obj = properties.remove("parent");
        if (obj != null) {
            parentComposite = (Composite) SwtUtils.getParentWidget(obj, properties);
            if (parentComposite == null && parent instanceof Composite) {
                parentComposite = (Composite) parent;
            }
        } else {
            parentComposite = (Composite) SwtUtils.getParentWidget(guiBuilder.getCurrent(),
                    properties);
        }
        guiBuilder.setCurrent(parentComposite);

        // dispose children
        Boolean rebuild = (Boolean) properties.remove("rebuild");
        if (parentComposite != null && rebuild != null && rebuild.booleanValue()) {
            SwtUtils.disposeChildren(parentComposite);
        }

        // run script
        Object result;
        try {
            result = runScript(src, parentComposite, binding);
        } catch (Exception e) {
            throw new GroovyException(e.getMessage());
        }
        
        if (result == null) {
            throw new NullPointerException("Script returns null: " + src);
        }
        return result;
    }

    /**
     * @param widget
     * @param script
     * @param parent
     * @return
     * @throws ScriptException
     * @throws ResourceException
     */
    private Object runScript(String script, Composite parent, Binding binding)
            throws GroovyException, ResourceException, ScriptException {

        // script binding
        binding.setVariable("guiBuilder", guiBuilder);
        if (parent != null) {
            binding.setVariable("parent", parent);
        }

        Object obj = guiBuilder.getScriptEngine().run(script, binding);

        // layout widget
        if (parent != null) {
            parent.layout();
        } else if (obj != null && (obj instanceof Composite)) {
            ((Composite) obj).layout();
        }

        return obj;
    }
}