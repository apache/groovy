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
package groovy.lang;

import groovy.ui.GroovyMain;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.*;
import java.lang.reflect.Constructor;
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
 * @version $Revision$
 */
public class GroovyShell extends GroovyObjectSupport {
       
    public static final String[] EMPTY_ARGS = {};

    
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
        this.loader = (GroovyClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new GroovyClassLoader(parentLoader,config);
            }
        });
        this.context = binding;        
        this.config = config;
    }
    
    public void initialiseBinding() {
        Map map = context.getVariables();
        if (map.get("shell")==null) map.put("shell",this);
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
                System.err.println("error: cannot compile file with .java extension: " + scriptName);
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
            scriptClass = (Class) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws CompilationFailedException, IOException {
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

        return runMainOrTestOrRunnable(scriptClass, args);

        // Set the context classloader back to what it was.
        //AccessController.doPrivileged(new DoSetContext(currentClassLoader));
    }

    /**
     * if (theClass has a main method) {
     * run the main method
     * } else if (theClass instanceof GroovyTestCase) {
     * use the test runner to run it
     * } else if (theClass implements Runnable) {
     * if (theClass has a constructor with String[] params)
     * instanciate theClass with this constructor and run
     * else if (theClass has a no-args constructor)
     * instanciate theClass with the no-args constructor and run
     * }
     */
    private Object runMainOrTestOrRunnable(Class scriptClass, String[] args) {
        if (scriptClass == null) {
            return null;
        }
        try {
            // let's find a main method
            scriptClass.getMethod("main", new Class[]{String[].class});
        } catch (NoSuchMethodException e) {
            // As no main() method was found, let's see if it's a unit test
            // if it's a unit test extending GroovyTestCase, run it with JUnit's TextRunner
            if (isUnitTestCase(scriptClass)) {
                return runTest(scriptClass);
            }
            // no main() method, not a unit test,
            // if it implements Runnable, try to instanciate it
            else if (Runnable.class.isAssignableFrom(scriptClass)) {
                Constructor constructor = null;
                Runnable runnable = null;
                Throwable reason = null;
                try {
                    // first, fetch the constructor taking String[] as parameter
                    constructor = scriptClass.getConstructor(new Class[]{(new String[]{}).getClass()});
                    try {
                        // instanciate a runnable and run it
                        runnable = (Runnable) constructor.newInstance(new Object[]{args});
                    } catch (Throwable t) {
                        reason = t;
                    }
                } catch (NoSuchMethodException e1) {
                    try {
                        // otherwise, find the default constructor
                        constructor = scriptClass.getConstructor(new Class[]{});
                        try {
                            // instanciate a runnable and run it
                            runnable = (Runnable) constructor.newInstance(new Object[]{});
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
                    throw new GroovyRuntimeException("This script or class could not be run. ", reason);
                }
            } else {
                throw new GroovyRuntimeException("This script or class could not be run. \n" +
                        "It should either: \n" +
                        "- have a main method, \n" +
                        "- be a class extending GroovyTestCase, \n" +
                        "- or implement the Runnable interface.");
            }
            return null;
        }
        // if that main method exist, invoke it
        return InvokerHelper.invokeMethod(scriptClass, "main", new Object[]{args});
    }

    /**
     * Run the specified class extending GroovyTestCase as a unit test.
     * This is done through reflection, to avoid adding a dependency to the JUnit framework.
     * Otherwise, developers embedding Groovy and using GroovyShell to load/parse/compile
     * groovy scripts and classes would have to add another dependency on their classpath.
     *
     * @param scriptClass the class to be run as a unit test
     */
    private Object runTest(Class scriptClass) {
        try {
            Object testSuite = InvokerHelper.invokeConstructorOf("junit.framework.TestSuite",new Object[]{scriptClass});
            return InvokerHelper.invokeStaticMethod("junit.textui.TestRunner", "run", new Object[]{testSuite});
        } catch (Exception e) {
            throw new GroovyRuntimeException("Failed to run the unit test. JUnit is not on the Classpath.");
        }
    }

    /**
     * Utility method to check through reflection if the parsed class extends GroovyTestCase.
     *
     * @param scriptClass the class we want to know if it extends GroovyTestCase
     * @return true if the class extends groovy.util.GroovyTestCase
     */
    private boolean isUnitTestCase(Class scriptClass) {
        // check if the parsed class is a GroovyTestCase,
        // so that it is possible to run it as a JUnit test
        boolean isUnitTestCase = false;
        try {
            try {
                Class testCaseClass = this.loader.loadClass("groovy.util.GroovyTestCase");
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
     * Runs the given script text with command line arguments
     *
     * @param scriptText is the text content of the script
     * @param fileName   is the logical file name of the script (which is used to create the class name of the script)
     * @param args       the command line arguments to pass in
     */
    public Object run(String scriptText, String fileName, String[] args) throws CompilationFailedException {
        try {
            return run(new ByteArrayInputStream(scriptText.getBytes(config.getSourceEncoding())), fileName, args);
        } catch (UnsupportedEncodingException e) {
            throw new CompilationFailedException(0, null, e);
        }
    }

    /**
     * Runs the given script with command line arguments
     *
     * @param in       the stream reading the script
     * @param fileName is the logical file name of the script (which is used to create the class name of the script)
     * @param args     the command line arguments to pass in
     */
    public Object run(final InputStream in, final String fileName, String[] args) throws CompilationFailedException {
        GroovyCodeSource gcs = (GroovyCodeSource) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new GroovyCodeSource(in, fileName, "/groovy/shell");
            }
        });
        Class scriptClass = parseClass(gcs);
        return runMainOrTestOrRunnable(scriptClass, args);
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
     * @return
     * @throws CompilationFailedException
     * @throws CompilationFailedException
     */
    public Object evaluate(GroovyCodeSource codeSource) throws CompilationFailedException {
        Script script = parse(codeSource);
        return script.run();
    }

    /**
     * Evaluates some script against the current Binding and returns the result
     *
     * @param scriptText the text of the script
     * @param fileName   is the logical file name of the script (which is used to create the class name of the script)
     */
    public Object evaluate(String scriptText, String fileName) throws CompilationFailedException {
        try {
            return evaluate(new ByteArrayInputStream(scriptText.getBytes(config.getSourceEncoding())), fileName);
        } catch (UnsupportedEncodingException e) {
            throw new CompilationFailedException(0, null, e);
        }
    }

    /**
     * Evaluates some script against the current Binding and returns the result.
     * The .class file created from the script is given the supplied codeBase
     */
    public Object evaluate(String scriptText, String fileName, String codeBase) throws CompilationFailedException {
        try {
            return evaluate(new GroovyCodeSource(new ByteArrayInputStream(scriptText.getBytes(config.getSourceEncoding())), fileName, codeBase));
        } catch (UnsupportedEncodingException e) {
            throw new CompilationFailedException(0, null, e);
        }
    }

    /**
     * Evaluates some script against the current Binding and returns the result
     *
     * @param file is the file of the script (which is used to create the class name of the script)
     */
    public Object evaluate(File file) throws CompilationFailedException, IOException {
        return evaluate(new GroovyCodeSource(file));
    }

    /**
     * Evaluates some script against the current Binding and returns the result
     *
     * @param scriptText the text of the script
     */
    public Object evaluate(String scriptText) throws CompilationFailedException {
        try {
            return evaluate(new ByteArrayInputStream(scriptText.getBytes(config.getSourceEncoding())), generateScriptName());
        } catch (UnsupportedEncodingException e) {
            throw new CompilationFailedException(0, null, e);
        }
    }

    /**
     * Evaluates some script against the current Binding and returns the result
     *
     * @param in the stream reading the script
     */
    public Object evaluate(InputStream in) throws CompilationFailedException {
        return evaluate(in, generateScriptName());
    }

    /**
     * Evaluates some script against the current Binding and returns the result
     *
     * @param in       the stream reading the script
     * @param fileName is the logical file name of the script (which is used to create the class name of the script)
     */
    public Object evaluate(InputStream in, String fileName) throws CompilationFailedException {
        Script script = null;
        try {
            script = parse(in, fileName);
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
     * @param in       the stream reading the script
     * @param fileName is the logical file name of the script (which is used to create the class name of the script)
     * @return the parsed script which is ready to be run via @link Script.run()
     */
    public Script parse(final InputStream in, final String fileName) throws CompilationFailedException {
        GroovyCodeSource gcs = (GroovyCodeSource) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new GroovyCodeSource(in, fileName, "/groovy/shell");
            }
        });
        return parse(gcs);
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
        return parse(new GroovyCodeSource(file));
    }

    /**
     * Parses the given script and returns it ready to be run
     *
     * @param scriptText the text of the script
     */
    public Script parse(String scriptText) throws CompilationFailedException {
        try {
            return parse(new ByteArrayInputStream(scriptText.getBytes(config.getSourceEncoding())), generateScriptName());
        } catch (UnsupportedEncodingException e) {
            throw new CompilationFailedException(0, null, e);
        }
    }

    public Script parse(String scriptText, String fileName) throws CompilationFailedException {
        try {
            return parse(new ByteArrayInputStream(scriptText.getBytes(config.getSourceEncoding())), fileName);
        } catch (UnsupportedEncodingException e) {
            throw new CompilationFailedException(0, null, e);
        }
    }

    /**
     * Parses the given script and returns it ready to be run
     *
     * @param in the stream reading the script
     */
    public Script parse(InputStream in) throws CompilationFailedException {
        return parse(in, generateScriptName());
    }

    protected synchronized String generateScriptName() {
        return "Script" + (++counter) + ".groovy";
    }
}
