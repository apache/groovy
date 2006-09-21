/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package groovy.util;


import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.NoBannerLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.helper.AntXMLContext;
import org.apache.tools.ant.helper.ProjectHelper2;
import org.codehaus.groovy.ant.FileScanner;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;
import groovy.xml.QName;

/**
 * Allows Ant tasks to be used with GroovyMarkup 
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>, changes by Dierk Koenig (dk)
 * @version $Revision$
 */
public class AntBuilder extends BuilderSupport {

    private static final Class[] addTaskParamTypes = { String.class };

    private Logger log = Logger.getLogger(getClass().getName());
    private Project project;
    private final AntXMLContext antXmlContext;
    private final ProjectHelper2.ElementHandler antElementHandler = new ProjectHelper2.ElementHandler();
    private final Target collectorTarget = new Target();
    private Object lastCompletedNode;



    public AntBuilder() {
        this(createProject());
    }

    public AntBuilder(final Project project) {
        this.project = project;
        
        antXmlContext = new AntXMLContext(project);
        collectorTarget.setProject(project);
        antXmlContext.setCurrentTarget(collectorTarget);
        antXmlContext.setLocator(new AntBuilderLocator());
        
        // FileScanner is a Groovy hack (utility?)
        project.addDataTypeDefinition("fileScanner", FileScanner.class);
    }

    // dk: introduced for convenience in subclasses
    protected Project getProject() {
        return project;
    }

    /**
     * @return Factory method to create new Project instances
     */
    protected static Project createProject() {
        Project project = new Project();
        BuildLogger logger = new NoBannerLogger();

        logger.setMessageOutputLevel(org.apache.tools.ant.Project.MSG_INFO);
        logger.setOutputPrintStream(System.out);
        logger.setErrorPrintStream(System.err);

        project.addBuildListener(logger);

        project.init();
        project.getBaseDir();
        return project;
    }

    protected void setParent(Object parent, Object child) {
    }

    
    /**
     * We don't want to return the node as created in {@link #createNode(Object, Map, Object)}
     * but the one made ready by {@link #nodeCompleted(Object, Object)}
     * @see groovy.util.BuilderSupport#doInvokeMethod(java.lang.String, java.lang.Object, java.lang.Object)
     */
    protected Object doInvokeMethod(String methodName, Object name, Object args) {
    	super.doInvokeMethod(methodName, name, args);
    	

    	// return the completed node
    	return lastCompletedNode;
    }

    /**
     * Determines, when the ANT Task that is represented by the "node" should perform.
     * Node must be an ANT Task or no "perform" is called.
     * If node is an ANT Task, it performs right after complete contstruction.
     * If node is nested in a TaskContainer, calling "perform" is delegated to that
     * TaskContainer.
     * @param parent note: null when node is root
     * @param node the node that now has all its children applied
     */
    protected void nodeCompleted(final Object parent, final Object node) {

    	antElementHandler.onEndElement(null, null, antXmlContext);

    	lastCompletedNode = node;
        if (parent != null) {
            log.finest("parent is not null: no perform on nodeCompleted");
            return; // parent will care about when children perform
        }
        
        // as in Target.execute()
        if (node instanceof Task) {
            Object task = node;
            // "Unwrap" the UnknownElement to return the real task to the calling code
            if (node instanceof UnknownElement) {
            	final UnknownElement unknownElement = (UnknownElement) node;
            	unknownElement.maybeConfigure();
                task = unknownElement.getRealThing();
            }
            
            lastCompletedNode = task;
            // UnknownElement may wrap everything: task, path, ...
            if (task instanceof Task) {
            	((Task) task).perform();
            }
        }
        else {
            final RuntimeConfigurable r = (RuntimeConfigurable) node;
            r.maybeConfigure(project);
        }
    }

    protected Object createNode(Object tagName) {
        return createNode(tagName.toString(), Collections.EMPTY_MAP);
    }

    protected Object createNode(Object name, Object value) {
        Object task = createNode(name);
        setText(task, value.toString());
        return task;
    }

    protected Object createNode(Object name, Map attributes, Object value) {
        Object task = createNode(name, attributes);
        setText(task, value.toString());
        return task;
    }
    
    /**
     * Builds an {@link Attributes} from a {@link Map}
     * @param attributes the attributes to wrap
     */
    protected static Attributes buildAttributes(final Map attributes) {
    	final AttributesImpl attr = new AttributesImpl();
    	for (final Iterator iter=attributes.entrySet().iterator(); iter.hasNext(); ) {
    		final Map.Entry entry = (Map.Entry) iter.next();
    		final String attributeName = (String) entry.getKey();
    		final String attributeValue = String.valueOf(entry.getValue());
    		attr.addAttribute(null, attributeName, attributeName, "CDATA", attributeValue);
    	}
    	return attr;
    }

    protected Object createNode(final Object name, final Map attributes) {

        String tagName = name.toString();
        String ns = "";

        if(name instanceof QName) {
            QName q = (QName)name;
            tagName = q.getLocalPart();
            ns = q.getNamespaceURI();
        }

        try
		{
			antElementHandler.onStartElement(ns, tagName, tagName, buildAttributes(attributes), antXmlContext);
		}
		catch (final SAXParseException e)
		{
            log.log(Level.SEVERE, "Caught: " + e, e);
		}
    	
		final RuntimeConfigurable wrapper = (RuntimeConfigurable) antXmlContext.getWrapperStack().lastElement();
    	return wrapper.getProxy();
    }

    protected void setText(Object task, String text) {
    	final char[] characters = text.toCharArray();
        try {
          	antElementHandler.characters(characters, 0, characters.length, antXmlContext);
        }
        catch (final SAXParseException e) {
            log.log(Level.WARNING, "SetText failed: " + task + ". Reason: " + e, e);
        }
    }

    public Project getAntProject() {
        return project;
    }
}

/**
 * Would be nice to retrieve location information (from AST?).
 * In a first time, without info
 */
class AntBuilderLocator implements Locator {
	public int getColumnNumber()
	{
		return 0;
	}
	public int getLineNumber()
	{
		return 0;
	}
	public String getPublicId()
	{
		return "";
	}
	public String getSystemId()
	{
		return "";
	}
}
