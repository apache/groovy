/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.util;

import groovy.xml.QName;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DemuxInputStream;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.NoBannerLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.dispatch.DispatchUtils;
import org.apache.tools.ant.helper.AntXMLContext;
import org.apache.tools.ant.helper.ProjectHelper2;
import org.codehaus.groovy.ant.FileScanner;
import org.codehaus.groovy.reflection.ReflectionUtils;
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Allows <a href="http://ant.apache.org/manual/coretasklist.html">Ant tasks</a> to
 * be used with a Groovy builder-style markup. Requires that {{ant.jar}} is on your classpath which will
 * happen automatically if you are using the Groovy distribution but will be up
 * to you to organize if you are embedding Groovy. If you wish to use the
 * <a href="http://ant.apache.org/manual/install#optionalTasks">optional tasks</a>
 * you will need to add one or more additional jars from the ant distribution to
 * your classpath - see the <a href="http://ant.apache.org/manual/install.html#librarydependencies">library
 * dependencies</a> for more details.
 */
@Deprecated
public class AntBuilder extends BuilderSupport {

    private final Logger log = Logger.getLogger(getClass().getName());
    private final Project project;
    private final AntXMLContext antXmlContext;
    private final ProjectHelper2.ElementHandler antElementHandler = new ProjectHelper2.ElementHandler();
    private final ProjectHelper2.TargetHandler antTargetHandler = new ProjectHelper2.TargetHandler();
    private final Target collectorTarget;
    private final Target implicitTarget;
    private Target definingTarget;
    private Object lastCompletedNode;
    // true when inside a task so special ant.target handling occurs just at top level
    boolean insideTask;

    private boolean saveStreams = true;
    private static Integer streamCount = 0;
    private static InputStream savedIn;
    private static PrintStream savedErr;
    private static PrintStream savedOut;
    private static DemuxInputStream demuxInputStream;
    private static DemuxOutputStream demuxOutputStream;
    private static DemuxOutputStream demuxErrorStream;
    private static InputStream savedProjectInputStream;

    public AntBuilder() {
        this(createProject());
    }

    public AntBuilder(final Project project) {
        this(project, new Target());
    }

    public AntBuilder(final Project project, final Target owningTarget) {
        this.project = project;

        /*
         * GROOVY-4524: The following is not needed anymore as an ant Project already by default has inputhandler
         * set to DefaultInputHandler. And if it is again set here, it mistakenly overrides the custom input handler
         * if set using -inputhandler switch. 
         */
        //this.project.setInputHandler(new DefaultInputHandler());

        collectorTarget = owningTarget;
        antXmlContext = new AntXMLContext(project);
        collectorTarget.setProject(project);
        antXmlContext.setCurrentTarget(collectorTarget);
        antXmlContext.setLocator(new AntBuilderLocator());
        antXmlContext.setCurrentTargets(new HashMap<String, Target>());

        implicitTarget = new Target();
        implicitTarget.setProject(project);
        implicitTarget.setName("");
        antXmlContext.setImplicitTarget(implicitTarget);

        // FileScanner is a Groovy utility
        project.addDataTypeDefinition("fileScanner", FileScanner.class);
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

    /**
     * #
     * Gets the Ant project in which the tasks are executed
     *
     * @return the project
     */
    public Project getProject() {
        return project;
    }

    /**
     * Gets the xml context of Ant used while creating tasks
     *
     * @return the Ant xml context
     */
    public AntXMLContext getAntXmlContext() {
        return antXmlContext;
    }

    /**
     * Whether stdin, stdout, stderr streams are saved.
     *
     * @return true if we are saving streams
     * @see #setSaveStreams(boolean)
     */
    public boolean isSaveStreams() {
        return saveStreams;
    }

    /**
     * Indicates that we save stdin, stdout, stderr and replace them
     * while AntBuilder is executing tasks with
     * streams that funnel the normal streams into Ant's logs.
     *
     * @param saveStreams set to false to disable this behavior
     */
    public void setSaveStreams(boolean saveStreams) {
        this.saveStreams = saveStreams;
    }

    /**
     * @return Factory method to create new Project instances
     */
    protected static Project createProject() {
        final Project project = new Project();

        final ProjectHelper helper = ProjectHelper.getProjectHelper();
        project.addReference(ProjectHelper.PROJECTHELPER_REFERENCE, helper);
        helper.getImportStack().addElement("AntBuilder"); // import checks that stack is not empty 

        final BuildLogger logger = new NoBannerLogger();

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
     *
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
     * If node is an ANT Task, it performs right after complete construction.
     * If node is nested in a TaskContainer, calling "perform" is delegated to that
     * TaskContainer.
     *
     * @param parent note: null when node is root
     * @param node   the node that now has all its children applied
     */
    protected void nodeCompleted(final Object parent, final Object node) {
        if (parent == null) insideTask = false;
        antElementHandler.onEndElement(null, null, antXmlContext);

        lastCompletedNode = node;
        if (parent != null && !(parent instanceof Target)) {
            log.finest("parent is not null: no perform on nodeCompleted");
            return; // parent will care about when children perform
        }
        if (definingTarget != null && definingTarget == parent && node instanceof Task) return; // inside defineTarget
        if (definingTarget == node) {
            definingTarget = null;
        }

        // as in Target.execute()
        if (node instanceof Task) {
            Task task = (Task) node;
            final String taskName = task.getTaskName();

            if ("antcall".equals(taskName) && parent == null) {
                throw new BuildException("antcall not supported within AntBuilder, consider using 'ant.project.executeTarget('targetName')' instead.");
            }

            if (saveStreams) {
                // save original streams
                synchronized (AntBuilder.class) {
                    int currentStreamCount = streamCount++;
                    if (currentStreamCount == 0) {
                        // we are first, save the streams
                        savedProjectInputStream = project.getDefaultInputStream();
                        savedIn = System.in;
                        savedErr = System.err;
                        savedOut = System.out;

                        if (!(savedIn instanceof DemuxInputStream)) {
                            project.setDefaultInputStream(savedIn);
                            demuxInputStream = new DemuxInputStream(project);
                            System.setIn(demuxInputStream);
                        }
                        demuxOutputStream = new DemuxOutputStream(project, false);
                        System.setOut(new PrintStream(demuxOutputStream));
                        demuxErrorStream = new DemuxOutputStream(project, true);
                        System.setErr(new PrintStream(demuxErrorStream));
                    }
                }
            }

            try {
                lastCompletedNode = performTask(task);
            } finally {
                if (saveStreams) {
                    synchronized (AntBuilder.class) {
                        int currentStreamCount = --streamCount;
                        if (currentStreamCount == 0) {
                            // last to leave, turn out the lights: restore original streams
                            project.setDefaultInputStream(savedProjectInputStream);
                            System.setOut(savedOut);
                            System.setErr(savedErr);
                            if (demuxInputStream != null) {
                                System.setIn(savedIn);
                                DefaultGroovyMethodsSupport.closeQuietly(demuxInputStream);
                                demuxInputStream = null;
                            }
                            DefaultGroovyMethodsSupport.closeQuietly(demuxOutputStream);
                            DefaultGroovyMethodsSupport.closeQuietly(demuxErrorStream);
                            demuxOutputStream = null;
                            demuxErrorStream = null;
                        }
                    }
                }
            }

            // restore dummy collector target
            if ("import".equals(taskName)) {
                antXmlContext.setCurrentTarget(collectorTarget);
            }
        } else if (node instanceof Target) {
            // restore dummy collector target
            antXmlContext.setCurrentTarget(collectorTarget);
        } else {
            final RuntimeConfigurable r = (RuntimeConfigurable) node;
            r.maybeConfigure(project);
        }
    }

    // Copied from org.apache.tools.ant.Task, since we need to get a real thing before it gets nulled in DispatchUtils.execute

    private Object performTask(Task task) {

        Throwable reason = null;
        try {
            // Have to call fireTestStared/fireTestFinished via reflection as they unfortunately have protected access in Project
            final Method fireTaskStarted = Project.class.getDeclaredMethod("fireTaskStarted", Task.class);
            ReflectionUtils.trySetAccessible(fireTaskStarted);
            fireTaskStarted.invoke(project, task);

            Object realThing;
            realThing = task;
            task.maybeConfigure();
            if (task instanceof UnknownElement) {
                realThing = ((UnknownElement) task).getRealThing();
            }

            DispatchUtils.execute(task);

            return realThing != null ? realThing : task;
        }
        catch (BuildException ex) {
            if (ex.getLocation() == Location.UNKNOWN_LOCATION) {
                ex.setLocation(task.getLocation());
            }
            reason = ex;
            throw ex;
        }
        catch (Exception ex) {
            reason = ex;
            BuildException be = new BuildException(ex);
            be.setLocation(task.getLocation());
            throw be;
        }
        catch (Error ex) {
            reason = ex;
            throw ex;
        }
        finally {
            try {
                final Method fireTaskFinished = Project.class.getDeclaredMethod("fireTaskFinished", Task.class, Throwable.class);
                ReflectionUtils.trySetAccessible(fireTaskFinished);
                fireTaskFinished.invoke(project, task, reason);
            }
            catch (Exception e) {
                BuildException be = new BuildException(e);
                be.setLocation(task.getLocation());
                throw be;
            }
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
        for (Object o : attributes.entrySet()) {
            final Map.Entry entry = (Map.Entry) o;
            final String attributeName = (String) entry.getKey();
            final String attributeValue = String.valueOf(entry.getValue());
            attr.addAttribute(null, attributeName, attributeName, "CDATA", attributeValue);
        }
        return attr;
    }

    protected Object createNode(final Object name, final Map attributes) {

        final Attributes attrs = buildAttributes(attributes);
        String tagName = name.toString();
        String ns = "";

        if (name instanceof QName) {
            QName q = (QName) name;
            tagName = q.getLocalPart();
            ns = q.getNamespaceURI();
        }

        // import can be used only as top level element
        if ("import".equals(name)) {
            antXmlContext.setCurrentTarget(implicitTarget);
        } else if ("target".equals(name) && !insideTask) {
            return onStartTarget(attrs, tagName, ns);
        } else if ("defineTarget".equals(name) && !insideTask) {
            return onDefineTarget(attrs, "target", ns);
        }

        try {
            antElementHandler.onStartElement(ns, tagName, tagName, attrs, antXmlContext);
        }
        catch (final SAXParseException e) {
            log.log(Level.SEVERE, "Caught: " + e, e);
        }

        insideTask = true;
        final RuntimeConfigurable wrapper = antXmlContext.getWrapperStack().lastElement();
        return wrapper.getProxy();
    }

    private Target onDefineTarget(final Attributes attrs, String tagName, String ns) {
        final Target target = new Target();
        target.setProject(project);
        target.setLocation(new Location(antXmlContext.getLocator()));
        try {
            antTargetHandler.onStartElement(ns, tagName, tagName, attrs, antXmlContext);
            final Target newTarget = getProject().getTargets().get(attrs.getValue("name"));
            antXmlContext.setCurrentTarget(newTarget);
            definingTarget = newTarget;
            return newTarget;
        }
        catch (final SAXParseException e) {
            log.log(Level.SEVERE, "Caught: " + e, e);
        }
        return null;
    }

    private Target onStartTarget(final Attributes attrs, String tagName, String ns) {
        final Target target = new Target();
        target.setProject(project);
        target.setLocation(new Location(antXmlContext.getLocator()));
        try {
            antTargetHandler.onStartElement(ns, tagName, tagName, attrs, antXmlContext);
            final Target newTarget = getProject().getTargets().get(attrs.getValue("name"));

            // execute dependencies (if any)
            final Vector<Target> targets = new Vector<Target>();
            for (final Enumeration<String> deps = newTarget.getDependencies(); deps.hasMoreElements();) {
                final String targetName = deps.nextElement();
                targets.add(project.getTargets().get(targetName));
            }
            getProject().executeSortedTargets(targets);

            antXmlContext.setCurrentTarget(newTarget);
            return newTarget;
        }
        catch (final SAXParseException e) {
            log.log(Level.SEVERE, "Caught: " + e, e);
        }
        return null;
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
@Deprecated
class AntBuilderLocator implements Locator {
    public int getColumnNumber() {
        return 0;
    }

    public int getLineNumber() {
        return 0;
    }

    public String getPublicId() {
        return "";
    }

    public String getSystemId() {
        return "";
    }
}
