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


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.NoBannerLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskAdapter;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.types.DataType;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Allows Ant tasks to be used with GroovyMarkup 
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class AntBuilder extends BuilderSupport {

    private static final Class[] addTaskParamTypes = { String.class };

    private Log log = LogFactory.getLog(getClass());
    private Project project;

    public AntBuilder() {
        this.project = createProject();
    }

    public AntBuilder(Project project) {
        this.project = project;
    }

    /**
     * @return Factory method to create new Project instances
     */
    protected Project createProject() {
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

    protected void nodeCompleted(Object parent, Object node) {
        if (node instanceof Task) {
            Task task = (Task) node;
            task.perform();
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

    protected Object createNode(Object name, Map attributes) {

        String tagName = name.toString();
        Object answer = null;

        Object parentObject = getCurrent();
        Object parentTask = getParentTask();

        // lets assume that Task instances are not nested inside other Task instances
        // for example <manifest> inside a <jar> should be a nested object, where as 
        // if the parent is not a Task the <manifest> should create a ManifestTask
        //
        // also its possible to have a root Ant tag which isn't a task, such as when
        // defining <fileset id="...">...</fileset>

        Object nested = null;
        if (parentObject != null && !(parentTask instanceof TaskContainer)) {
            nested = createNestedObject(parentObject, tagName);
        }

        Task task = null;
        if (nested == null) {
            task = createTask(tagName);
            if (task != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating an ant Task for name: " + tagName);
                }

                // the following algorithm follows the lifetime of a tag
                // http://jakarta.apache.org/ant/manual/develop.html#writingowntask
                // kindly recommended by Stefan Bodewig

                // create and set its project reference
                if (task instanceof TaskAdapter) {
                    answer = ((TaskAdapter) task).getProxy();
                }
                else {
                    answer = task;
                }

                // set the task ID if one is given
                Object id = attributes.remove("id");
                if (id != null) {
                    project.addReference((String) id, task);
                }

                // now lets initialize
                task.init();

                // now lets set any attributes of this tag...
                setBeanProperties(task, attributes);
            }
        }

        if (task == null) {
            if (nested == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to create a data type for tag: " + tagName);
                }
                nested = createDataType(tagName);
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("Created nested property tag: " + tagName);
                }
            }

            if (nested != null) {
                answer = nested;

                // set the task ID if one is given
                Object id = attributes.remove("id");
                if (id != null) {
                    project.addReference((String) id, nested);
                }

                try {
                    InvokerHelper.setProperty(nested, "name", tagName);
                }
                catch (Exception e) {
                }

                // now lets set any attributes of this tag...
                setBeanProperties(nested, attributes);

                // now lets add it to its parent
                if (parentObject != null) {
                    IntrospectionHelper ih = IntrospectionHelper.getHelper(parentObject.getClass());
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug(
                                "About to set the: "
                                    + tagName
                                    + " property on: "
                                    + parentObject
                                    + " to value: "
                                    + nested
                                    + " with type: "
                                    + nested.getClass());
                        }

                        ih.storeElement(project, parentObject, nested, (String) tagName);
                    }
                    catch (Exception e) {
                        log.warn("Caught exception setting nested: " + tagName, e);
                    }

                    // now try to set the property for good measure
                    // as the storeElement() method does not
                    // seem to call any setter methods of non-String types
                    try {
                        InvokerHelper.setProperty(parentObject, tagName, nested);
                    }
                    catch (Exception e) {
                        log.debug("Caught exception trying to set property: " + tagName + " on: " + parentObject);
                    }
                }
            }
            else {
                log.warn("Could not convert tag: " + tagName + " into an Ant task, data type or property");
            }
        }

        return answer;
    }

    protected void setText(Object task, String text) {
        // now lets set the addText() of the body content, if its applicaable
        Method method = getAccessibleMethod(task.getClass(), "addText", addTaskParamTypes);
        if (method != null) {
            Object[] args = { text };
            try {
                method.invoke(task, args);
            }
            catch (Exception e) {
                log.warn("Cannot call addText on: " + task + ". Reason: " + e, e);
            }
        }
    }

    protected Method getAccessibleMethod(Class theClass, String name, Class[] paramTypes) {
        while (true) {
            try {
                Method answer = theClass.getDeclaredMethod(name, paramTypes);
                if (answer != null) {
                    return answer;
                }
            }
            catch (Exception e) {
                // ignore
            }
            theClass = theClass.getSuperclass();
            if (theClass == null) {
                return null;
            }
        }
    }

    public Project getAntProject() {
        return project;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void setBeanProperties(Object object, Map map) {
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            setBeanProperty(object, name, value);
        }
    }

    protected void setBeanProperty(Object object, String name, Object value) {
        if (log.isDebugEnabled()) {
            log.debug("Setting bean property on: " + object + " name: " + name + " value: " + value);
        }

        IntrospectionHelper ih = IntrospectionHelper.getHelper(object.getClass());

        if (value instanceof String) {
            try {
                ih.setAttribute(getAntProject(), object, name.toLowerCase(), (String) value);
                return;
            }
            catch (Exception e) {
                // ignore: not a valid property
            }
        }

        try {

            ih.storeElement(getAntProject(), object, value, name);
        }
        catch (Exception e) {

            InvokerHelper.setProperty(object, name, value);
        }
    }

    /**
     * Creates a nested object of the given object with the specified name
     */
    protected Object createNestedObject(Object object, String name) {
        Object dataType = null;
        if (object != null) {
            IntrospectionHelper ih = IntrospectionHelper.getHelper(object.getClass());

            if (ih != null) {
                try {
                    dataType = ih.createElement(getAntProject(), object, name.toLowerCase());
                }
                catch (BuildException be) {
                    log.error(be);
                }
            }
        }
        if (dataType == null) {
            dataType = createDataType(name);
        }
        return dataType;
    }

    protected Object createDataType(String name) {
        Object dataType = null;

        Class type = (Class) getAntProject().getDataTypeDefinitions().get(name);

        if (type != null) {

            Constructor ctor = null;
            boolean noArg = false;

            // DataType can have a "no arg" constructor or take a single
            // Project argument.
            try {
                ctor = type.getConstructor(new Class[0]);
                noArg = true;
            }
            catch (NoSuchMethodException nse) {
                try {
                    ctor = type.getConstructor(new Class[] { Project.class });
                    noArg = false;
                }
                catch (NoSuchMethodException nsme) {
                    log.info("datatype '" + name + "' didn't have a constructor with an Ant Project", nsme);
                }
            }

            if (noArg) {
                dataType = createDataType(ctor, new Object[0], name, "no-arg constructor");
            }
            else {
                dataType = createDataType(ctor, new Object[] { getAntProject()}, name, "an Ant project");
            }
            if (dataType != null) {
                ((DataType) dataType).setProject(getAntProject());
            }
        }

        return dataType;
    }

    /**
     * @return an object create with the given constructor and args.
     * @param ctor a constructor to use creating the object
     * @param args the arguments to pass to the constructor
     * @param name the name of the data type being created
     * @param argDescription a human readable description of the args passed
     */
    protected Object createDataType(Constructor ctor, Object[] args, String name, String argDescription) {
        try {
            Object datatype = ctor.newInstance(args);
            return datatype;
        }
        catch (InstantiationException ie) {
            log.error("datatype '" + name + "' couldn't be created with " + argDescription, ie);
        }
        catch (IllegalAccessException iae) {
            log.error("datatype '" + name + "' couldn't be created with " + argDescription, iae);
        }
        catch (InvocationTargetException ite) {
            log.error("datatype '" + name + "' couldn't be created with " + argDescription, ite);
        }
        return null;
    }

    /**
     * @param taskName
     * @return
     * @throws JellyTagException
     */
    protected Task createTask(String taskName) {
        return createTask(taskName, (Class) getAntProject().getTaskDefinitions().get(taskName));
    }

    protected Task createTask(String taskName, Class taskType) {
        if (taskType == null) {
            return null;
        }
        try {
            Object o = taskType.newInstance();
            Task task = null;
            if (o instanceof Task) {
                task = (Task) o;
            }
            else {
                TaskAdapter taskA = new TaskAdapter();
                taskA.setProxy(o);
                task = taskA;
            }

            task.setProject(getAntProject());
            task.setTaskName(taskName);

            return task;
        }
        catch (Exception e) {
            log.warn("Could not create task: " + taskName + ". Reason: " + e, e);
            return null;
        }
    }

    protected Task getParentTask() {
        Object current = getCurrent();
        if (current instanceof Task) {
            return (Task) current;
        }
        return null;
    }
}
