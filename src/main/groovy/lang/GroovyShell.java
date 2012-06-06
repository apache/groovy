/*
 * Copyright 2003-2012 the original author or authors.
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
package groovy.lang;

import groovy.ui.GroovyMain;
import groovy.security.GroovyCodeSourcePermission;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.plugin.GroovyRunner;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Map;

/**
 * Represents a groovy shell capable of running arbitrary groovy scripts
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Guillaume Laforge
 * @author Paul King
 * @version $Revision$
 */
public class GroovyShell extends GroovyObjectSupport {

    public static final String DEFAULT_CODE_BASE = "/groovy/shell";

    private Binding context;
    private int counter;
    private CompilerConfiguration config;
    private GroovyClassLoader loader;

    public static void main(String[] args) {
        GroovyMain.main(args);
    }

    public GroovyShell() {
        this(null, new Binding());
    }

    public GroovyShell(Binding binding) {
        this(null, binding);
    }

    public GroovyShell(CompilerConfiguration config) {
        this(new Binding(), config);
    }

    public GroovyShell(Binding binding, CompilerConfiguration config) {
        this(null, binding, config);
    }

    public GroovyShell(ClassLoader parent, Binding binding) {
        this(parent, binding, CompilerConfiguration.DEFAULT);
    }

    public GroovyShell(ClassLoader parent) {
        this(parent, new Binding(), CompilerConfiguration.DEFAULT);
    }
    
    public GroovyShell(ClassLoader parent, Binding binding, final CompilerConfiguration config) {
        if (binding == null) {
            throw new IllegalArgumentException("Binding must not be null.");
        }
        if (config == null) {
            throw new IllegalArgumentException("Compiler configuration must not be null.");
        }
        final ClassLoader parentLoader = (parent!=null)?parent:GroovyShell.class.getClassLoader();
        this.loader = AccessController.doPrivileged(new PrivilegedAction<GroovyClassLoader>() {
            public GroovyClassLoader run() {
                return new GroovyClassLoader(parentLoader,config);
            }
        });
        this.context = binding;        
        this.config = config;
    }
    
    public void resetLoadedClasses() {
        loader.clearCache();
    }

    /**
     * Creates a child shell using a new ClassLoader which uses the parent shell's
     * class loader as its parent
     *
     * @param shell is the parent shell used for the variable bindings and the parent class loader
     */
    public GroovyShell(GroovyShell shell) {
        this(shell.loader, shell.context);
    }

    public Binding getContext() {
        return context;
    }

    public GroovyClassLoader getClassLoader() {
        return loader;
    }

    public Object getProperty(String property) {
        Object answer = getVariable(property);
        if (answer == null) {
            answer = super.getProperty(property);
        }
        return answer;
    }

    public void setProperty(String property, Object newValue) {
        setVariable(property, newValue);
        try {
            super.setProperty(property, newValue);
        } catch (GroovyRuntimeException e) {
            // ignore, was probably a dynamic property
        }
    }

    //
    // FIXME: Use List<String> here, current version is not safe
    //

    /**
     * A helper method which runs the given script file with the given command line arguments
     *
     * @param scriptFile the file of the script to run
     * @param list       the command line arguments to pass in
     */
    public Object run(File scriptFile, List list) throws CompilationFailedException, IOException {
        String[] args = new String[list.size()];
        return run(scriptFile, (String[]) list.toArray(args));
    }

    /**
     * A helper method which runs the given cl script with the given command line arguments
     *
     * @param scriptText is the text content of the script
     * @param fileName   is the logical file name of the script (which is used to create the class name of the script)
     * @param list       the command line arguments to pass in
     */
    public Object run(String scriptText, String fileName, List list) throws CompilationFailedException {
        String[] args = new String[list.size()];
        list.toArray(args);
        return run(scriptText, fileName, args);
    }

    /**
     * Runs the given script file name with the given command line arguments
     *
     * @param scriptFile the file name of the script to run
     * @param args       the command line arguments to pass in
     */
    public Object run(final File scriptFile, String[] args) throws CompilationFailedException, IOException {
        String scriptName = scriptFile.getName();
        int p = scriptName.lastIndexOf(".");
        if (p++ >= 0) {
            if (scriptName.substring(p).equals("java")) {
                throw new CompilationFailedException(0, null);
            }
        }

        // Get the current context classloader and save it on the stack
        final Thread thread = Thread.currentThread();
        //ClassLoader currentClassLoader = thread.getContextClassLoader();

        class DoSetContext implements PrivilegedAction {
            ClassLoader classLoader;

            public DoSetContext(ClassLoader loader) {
                classLoader = loader;
            }

            public Object run() {
                thread.setContextClassLoader(classLoader);
                return null;
            }
        }

        AccessController.doPrivileged(new DoSetContext(loader));

        // Parse the script, generate the class, and invoke the main method.  This is a little looser than
        // if you are compiling the script because the JVM isn't executing the main method.
        Class scriptClass;
        try {
            scriptClass = AccessController.doPrivileged(new PrivilegedExceptionAction<Class>() {
                public Class run() throws CompilationFailedException, IOException {
                    return loader.parseClass(scriptFile);
                }
            });
        } catch (PrivilegedActionException pae) {
            Exception e = pae.getException();
            if (e instanceof CompilationFailedException) {
                throw (CompilationFailedException) e;
            } else if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw (RuntimeException) pae.getException();
            }
        }

        return runScriptOrMainOrTestOrRunnable(scriptClass, args);

        // Set the context classloader back to what it was.
        //AccessController.doPrivileged(new DoSetContext(currentClassLoader));
    }

    /**
     * if (theClass is a Script) {
     * run it like a script
     * } else if (theClass has a main method) {
     * run the main method
     * } else if (theClass instanceof GroovyTestCase) {
     * use the test runner to run it
     * } else if (theClass implements Runnable) {
     * if (theClass has a constructor with String[] params)
     * instantiate theClass with this constructor and run
     * else if (theClass has a no-args constructor)
     * instantiate theClass with the no-args constructor and run
     * }
     */
    private Object runScriptOrMainOrTestOrRunnable(Class scriptClass, String[] args) {
        if (scriptClass == null) {
            return null;
        }
        if (Script.class.isAssignableFrom(scriptClass)) {
            // treat it just like a script if it is one
            Script script  = null;
            try {
                script = (Script) scriptClass.newInstance();
            } catch (InstantiationException e) {
                // ignore instantiation errors,, try to do main
            } catch (IllegalAccessException e) {
               // ignore instantiation errors, try to do main
            }
            if (script != null) {
                script.setBinding(context);
                script.setProperty("args", args);
                return script.run();
            }
        }
        try {
            // let's find a main method
            scriptClass.getMethod("main", new Class[]{String[].class});
            // if that main method exist, invoke it
            return InvokerHelper.invokeMethod(scriptClass, "main", new Object[]{args});
        } catch (NoSuchMethodException e) {
            // if it implements Runnable, try to instantiate it
            if (Runnable.class.isAssignableFrom(scriptClass)) {
                return runRunnable(scriptClass, args);
            }
            // if it's a JUnit 3.8.x test, run it with an appropriate runner
            if (isJUnit3Test(scriptClass)) {
                return runJUnit3Test(scriptClass);
            }
            // if it's a JUnit 3.8.x test suite, run it with an appropriate runner
            if (isJUnit3TestSuite(scriptClass)) {
                return runJUnit3TestSuite(scriptClass);
            }
            // if it's a JUnit 4.x test, run it with an appropriate runner
            if (isJUnit4Test(scriptClass)) {
                return runJUnit4Test(scriptClass);
            }
            for (Map.Entry<String, GroovyRunner> entry : GroovySystem.RUNNER_REGISTRY.entrySet()) {
                GroovyRunner runner = entry.getValue();
                if (runner != null && runner.canRun(scriptClass, this.loader)) {
                    return runner.run(scriptClass, this.loader);
                }
            }
            String message = "This script or class could not be run.\n" +
                    "It should either:\n" +
                    "- have a main method,\n" +
                    "- be a JUnit test or extend GroovyTestCase,\n" +
                    "- implement the Runnable interface,\n" +
                    "- or be compatible with a registered script runner. Known runners:\n";
            if (GroovySystem.RUNNER_REGISTRY.isEmpty()) {
                message += "  * <none>";
            }
            for (Map.Entry<String, GroovyRunner> entry : GroovySystem.RUNNER_REGISTRY.entrySet()) {
                message += "  * " + entry.getKey() + "\n";
            }
            throw new GroovyRuntimeException(message);
        }
    }

    private Object runRunnable(Class scriptClass, String[] args) {
        Constructor constructor = null;
        Runnable runnable = null;
        Throwable reason = null;
        try {
            // first, fetch the constructor taking String[] as parameter
            constructor = scriptClass.getConstructor(new Class[]{(new String[]{}).getClass()});
            try {
                // instantiate a runnable and run it
                runnable = (Runnable) constructor.newInstance(new Object[]{args});
            } catch (Throwable t) {
                reason = t;
            }
        } catch (NoSuchMethodException e1) {
            try {
                // otherwise, find the default constructor
                constructor = scriptClass.getConstructor(new Class[]{});
                try {
                    // instantiate a runnable and run it
                    runnable = (Runnable) constructor.newInstance();
                } catch (InvocationTargetException ite) {
                    throw new InvokerInvocationException(ite.getTargetException());
                } catch (Throwable t) {
                    reason = t;
                }
            } catch (NoSuchMethodException nsme) {
                reason = nsme;
            }
        }
        if (constructor != null && runnable != null) {
            runnable.run();
        } else {
            throw new GroovyRuntimeException("This script or class was runnable but could not be run. ", reason);
        }
        return null;
    }

    /**
     * Run the specified class extending TestCase as a unit test.
     * This is done through reflection, to avoid adding a dependency to the JUnit framework.
     * Otherwise, developers embedding Groovy and using GroovyShell to load/parse/compile
     * groovy scripts and classes would have to add another dependency on their classpath.
     *
     * @param scriptClass the class to be run as a unit test
     */
    private Object runJUnit3Test(Class scriptClass) {
        try {
            Object testSuite = InvokerHelper.invokeConstructorOf("junit.framework.TestSuite", new Object[]{scriptClass});
            return InvokerHelper.invokeStaticMethod("junit.textui.TestRunner", "run", new Object[]{testSuite});
        } catch (ClassNotFoundException e) {
            throw new GroovyRuntimeException("Failed to run the unit test. JUnit is not on the Classpath.", e);
        }
    }

    /**
     * Run the specified class extending TestSuite as a unit test.
     * This is done through reflection, to avoid adding a dependency to the JUnit framework.
     * Otherwise, developers embedding Groovy and using GroovyShell to load/parse/compile
     * groovy scripts and classes would have to add another dependency on their classpath.
     *
     * @param scriptClass the class to be run as a unit test
     */
    private Object runJUnit3TestSuite(Class scriptClass) {
        try {
            Object testSuite = InvokerHelper.invokeStaticMethod(scriptClass, "suite", new Object[]{});
            return InvokerHelper.invokeStaticMethod("junit.textui.TestRunner", "run", new Object[]{testSuite});
        } catch (ClassNotFoundException e) {
            throw new GroovyRuntimeException("Failed to run the unit test. JUnit is not on the Classpath.", e);
        }
    }

    private Object runJUnit4Test(Class scriptClass) {
        try {
            return InvokerHelper.invokeStaticMethod("org.codehaus.groovy.vmplugin.v5.JUnit4Utils",
                    "realRunJUnit4Test", new Object[]{scriptClass, this.loader});
        } catch (ClassNotFoundException e) {
            throw new GroovyRuntimeException("Failed to run the JUnit 4 test.", e);
        }
    }

    /**
     * Utility method to check through reflection if the class appears to be a
     * JUnit 3.8.x test, i.e.&nsbp;checks if it extends JUnit 3.8.x's TestCase.
     *
     * @param scriptClass the class we want to check
     * @return true if the class appears to be a test
     */
    private boolean isJUnit3Test(Class scriptClass) {
        // check if the parsed class is a GroovyTestCase,
        // so that it is possible to run it as a JUnit test
        boolean isUnitTestCase = false;
        try {
            try {
                Class testCaseClass = this.loader.loadClass("junit.framework.TestCase");
                // if scriptClass extends testCaseClass
                if (testCaseClass.isAssignableFrom(scriptClass)) {
                    isUnitTestCase = true;
                }
            } catch (ClassNotFoundException e) {
                // fall through
            }
        } catch (Throwable e) {
            // fall through
        }
        return isUnitTestCase;
    }

     /**
     * Utility method to check through reflection if the class appears to be a
     * JUnit 3.8.x test suite, i.e.&nsbp;checks if it extends JUnit 3.8.x's TestSuite.
     *
     * @param scriptClass the class we want to check
     * @return true if the class appears to be a test
     */
    private boolean isJUnit3TestSuite(Class scriptClass) {
        // check if the parsed class is a TestSuite,
        // so that it is possible to run it as a JUnit test
        boolean isUnitTestSuite = false;
        try {
            try {
                Class testSuiteClass = this.loader.loadClass("junit.framework.TestSuite");
                // if scriptClass extends TestSuiteClass
                if (testSuiteClass.isAssignableFrom(scriptClass)) {
                    isUnitTestSuite = true;
                }
            } catch (ClassNotFoundException e) {
                // fall through
            }
        } catch (Throwable e) {
            // fall through
        }
        return isUnitTestSuite;
    }

    /**
     * Utility method to check via reflection if the parsed class appears to be a JUnit4
     * test, i.e.&nsbp;checks whether it appears to be using the relevant JUnit 4 annotations.
     *
     * @param scriptClass the class we want to check
     * @return true if the class appears to be a test
     */
    private boolean isJUnit4Test(Class scriptClass) {
        // if we are running under Java 1.4 don't bother trying to check
        char version = System.getProperty("java.version").charAt(2);
        if (version < '5') {
            return false;
        }

        // check if there are appropriate class or method annotations
        // that suggest we have a JUnit 4 test
        boolean isTest = false;

        try {
            if (InvokerHelper.invokeStaticMethod("org.codehaus.groovy.vmplugin.v5.JUnit4Utils",
                    "realIsJUnit4Test", new Object[]{scriptClass, this.loader}) == Boolean.TRUE) {
                isTest = true;
            }
        } catch (ClassNotFoundException e) {
            throw new GroovyRuntimeException("Failed to invoke the JUnit 4 helper class.", e);
        }
        return isTest;
    }

    /**
     * Runs the given script text with command line arguments
     *
     * @param scriptText is the text content of the script
     * @param fileName   is the logical file name of the script (which is used to create the class name of the script)
     * @param args       the command line arguments to pass in
     */
    public Object run(final String scriptText, final String fileName, String[] args) throws CompilationFailedException {
        GroovyCodeSource gcs = AccessController.doPrivileged(new PrivilegedAction<GroovyCodeSource>() {
            public GroovyCodeSource run() {
                return new GroovyCodeSource(scriptText, fileName, DEFAULT_CODE_BASE);
            }
        });
        Class scriptClass = parseClass(gcs);
        return runScriptOrMainOrTestOrRunnable(scriptClass, args);
    }

    /**
     * Runs the given script with command line arguments
     *
     * @param in       the stream reading the script
     * @param fileName is the logical file name of the script (which is used to create the class name of the script)
     * @param args     the command line arguments to pass in
     */
    public Object run(final Reader in, final String fileName, String[] args) throws CompilationFailedException {
        GroovyCodeSource gcs = AccessController.doPrivileged(new PrivilegedAction<GroovyCodeSource>() {
                    public GroovyCodeSource run() {
                        return new GroovyCodeSource(in, fileName, DEFAULT_CODE_BASE);
                    }
        });
        Class scriptClass = parseClass(gcs);
        return runScriptOrMainOrTestOrRunnable(scriptClass, args);
    }

    public Object getVariable(String name) {
        return context.getVariables().get(name);
    }

    public void setVariable(String name, Object value) {
        context.setVariable(name, value);
    }

    /**
     * Evaluates some script against the current Binding and returns the result
     *
     * @param codeSource
     * @throws CompilationFailedException
     */
    public Object evaluate(GroovyCodeSource codeSource) throws CompilationFailedException {
        Script script = parse(codeSource);
        script.setBinding(context);
        return script.run();
    }

    /**
     * Evaluates some script against the current Binding and returns the result
     *
     * @param scriptText the text of the script
     */
    public Object evaluate(final String scriptText) throws CompilationFailedException {
        return evaluate(scriptText, generateScriptName(), DEFAULT_CODE_BASE);
    }

    /**
     * Evaluates some script against the current Binding and returns the result
     *
     * @param scriptText the text of the script
     * @param fileName   is the logical file name of the script (which is used to create the class name of the script)
     */
    public Object evaluate(String scriptText, String fileName) throws CompilationFailedException {
        return evaluate(scriptText, fileName, DEFAULT_CODE_BASE);
    }

    /**
     * Evaluates some script against the current Binding and returns the result.
     * The .class file created from the script is given the supplied codeBase
     */
    public Object evaluate(final String scriptText, final String fileName, final String codeBase) throws CompilationFailedException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new GroovyCodeSourcePermission(codeBase));
        }

        GroovyCodeSource gcs = AccessController.doPrivileged(new PrivilegedAction<GroovyCodeSource>() {
            public GroovyCodeSource run() {
                return new GroovyCodeSource(scriptText, fileName, codeBase);
            }
        });

        return evaluate(gcs);
    }

    /**
     * Evaluates some script against the current Binding and returns the result
     *
     * @param file is the file of the script (which is used to create the class name of the script)
     */
    public Object evaluate(File file) throws CompilationFailedException, IOException {
        return evaluate(new GroovyCodeSource(file, config.getSourceEncoding()));
    }

    /**
     * Evaluates some script against the current Binding and returns the result
     *
     * @param in the stream reading the script
     */
    public Object evaluate(Reader in) throws CompilationFailedException {
        return evaluate(in, generateScriptName());
    }

    /**
     * Evaluates some script against the current Binding and returns the result
     *
     * @param in       the stream reading the script
     * @param fileName is the logical file name of the script (which is used to create the class name of the script)
     */
    public Object evaluate(Reader in, String fileName) throws CompilationFailedException {
        Script script = null;
        try {
            script = parse(in, fileName);
            script.setBinding(context);
            return script.run();
        } finally {
            if (script != null) {
                InvokerHelper.removeClass(script.getClass());
            }
        }
    }


    /**
     * Parses the given script and returns it ready to be run
     *
     * @param reader    the stream reading the script
     * @param fileName  is the logical file name of the script (which is used to create the class name of the script)
     * @return the parsed script which is ready to be run via {@link Script#run()}
     */
    public Script parse(final Reader reader, final String fileName) throws CompilationFailedException {
        return parse(new GroovyCodeSource(reader, fileName, DEFAULT_CODE_BASE));
    }

    /**
     * Parses the groovy code contained in codeSource and returns a java class.
     */
    private Class parseClass(final GroovyCodeSource codeSource) throws CompilationFailedException {
        // Don't cache scripts
        return loader.parseClass(codeSource, false);
    }

    /**
     * Parses the given script and returns it ready to be run.  When running in a secure environment
     * (-Djava.security.manager) codeSource.getCodeSource() determines what policy grants should be
     * given to the script.
     *
     * @param codeSource
     * @return ready to run script
     */
    public Script parse(final GroovyCodeSource codeSource) throws CompilationFailedException {
        return InvokerHelper.createScript(parseClass(codeSource), context);
    }

    /**
     * Parses the given script and returns it ready to be run
     *
     * @param file is the file of the script (which is used to create the class name of the script)
     */
    public Script parse(File file) throws CompilationFailedException, IOException {
        return parse(new GroovyCodeSource(file, config.getSourceEncoding()));
    }

    /**
     * Parses the given script and returns it ready to be run
     *
     * @param scriptText the text of the script
     */
    public Script parse(String scriptText) throws CompilationFailedException {
        return parse(scriptText, generateScriptName());
    }

    public Script parse(final String scriptText, final String fileName) throws CompilationFailedException {
        GroovyCodeSource gcs = AccessController.doPrivileged(new PrivilegedAction<GroovyCodeSource>() {
            public GroovyCodeSource run() {
                return new GroovyCodeSource(scriptText, fileName, DEFAULT_CODE_BASE);
            }
        });
        return parse(gcs);
    }

    /**
     * Parses the given script and returns it ready to be run
     *
     * @param in the stream reading the script
     */
    public Script parse(Reader in) throws CompilationFailedException {
        return parse(in, generateScriptName());
    }

    protected synchronized String generateScriptName() {
        return "Script" + (++counter) + ".groovy";
    }
}
