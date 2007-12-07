/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.util;


import groovy.xml.QName;
import org.apache.tools.ant.*;
import org.apache.tools.ant.helper.AntXMLContext;
import org.apache.tools.ant.helper.ProjectHelper2;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.codehaus.groovy.ant.FileScanner;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Allows Ant tasks to be used with GroovyMarkup 
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>, changes by Dierk Koenig (dk)
 * @version $Revision$
 */
public class AntBuilder extends BuilderSupport {

    private static final Class[] ADD_TASK_PARAM_TYPES = { String.class };

    private final Logger log = Logger.getLogger(getClass().getName());
    private Project project;
    private final AntXMLContext antXmlContext;
    private final ProjectHelper2.ElementHandler antElementHandler = new ProjectHelper2.ElementHandler();
    private final Target collectorTarget;
    private Object lastCompletedNode;



    public AntBuilder() {
        this(createProject());
    }

    public AntBuilder(final Project project) {
        this(project, new Target());
    }

    public AntBuilder(final Project project, final Target owningTarget) {
        this.project = project;

        this.project.setDefaultInputStream(System.in);
        this.project.setInputHandler(new DefaultInputHandler());

        collectorTarget = owningTarget;
        antXmlContext = new AntXMLContext(project);
        collectorTarget.setProject(project);
        antXmlContext.setCurrentTarget(collectorTarget);
        antXmlContext.setLocator(new AntBuilderLocator());
        
        // FileScanner is a Groovy hack (utility?)
        project.addDataTypeDefinition("fileScanner", FileScanner.class);
    }

    /**
     * <p>
     * Set the input stream handling policy.
     * This method can also be used to define a different input stream for text input.
     * </p>
     *
     * <p>
     * <b>Remark</b>: This policy has been introduced to fix GROOVY-2362.
     * With the upgrade to Ant 1.7 from Ant 1.6.5, it was not possible to call the input task twice
     * without getting errors. By using a default input handler and a DemuxInputStream,
     * it seems to solve the problem.
     * </p>
     *
     * @param in input stream to use for reading input from the command-line
     */
    public void setInputStreamHandlingPolicy(InputStream in) {
        this.project.setDefaultInputStream(in);
        this.project.setInputHandler(new DefaultInputHandler());
        System.setIn(new DemuxInputStream(this.project));
    }

    public AntBuilder(final Task parentTask) {
    	this(parentTask.getProject(), parentTask.getOwningTarget());
    	
    	// define "owning" task as wrapper to avoid having tasks added to the target
    	// but it needs to be an UnknownElement and no access is available from
    	// task to its original UnknownElement 
        final UnknownElement ue = new UnknownElement(parentTask.getTaskName());
        ue.setProject(parentTask.getProject());
        ue.setTaskType(parentTask.getTaskType());
        ue.setTaskName(parentTask.getTaskName());
        ue.setLocation(parentTask.getLocation());
        ue.setOwningTarget(parentTask.getOwningTarget());
        ue.setRuntimeConfigurableWrapper(parentTask.getRuntimeConfigurableWrapper());
        parentTask.getRuntimeConfigurableWrapper().setProxy(ue);
    	antXmlContext.pushWrapper(parentTask.getRuntimeConfigurableWrapper());
    }

    /**#
     * Gets the Ant project in which the tasks are executed
     * @return the project
     */
    public Project getProject() {
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
                // save original input stream
                InputStream originalIn = System.in;
                DemuxInputStream inputStream = new DemuxInputStream(this.project);
                System.setIn(inputStream);

                ((Task) task).perform();

                // restore original input stream
                System.setIn(originalIn);
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException ioe) {
                    log.log(Level.WARNING, "The wrapper input stream around System.in couldn't be closed.");
                }
            }
        }
        else {
            final RuntimeConfigurable r = (RuntimeConfigurable) node;
            r.maybeConfigure(project);
        }
    }

    protected Object createNode(Object tagName) {
        return createNode(tagName, Collections.EMPTY_MAP);
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
     * 
     * @param attributes the attributes to wrap
     * @return the wrapped attributes
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
