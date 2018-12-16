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
package groovy.lang;

import groovy.security.GroovyCodeSourcePermission;
import groovy.ui.GroovyMain;
import org.apache.groovy.plugin.GroovyRunner;
import org.apache.groovy.plugin.GroovyRunnerRegistry;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.codehaus.groovy.runtime.InvokerHelper.MAIN_METHOD_NAME;

/**
 * Represents a groovy shell capable of running arbitrary groovy scripts
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Guillaume Laforge
 * @author Paul King
 */
public class GroovyShell extends GroovyObjectSupport {

    public static final String DEFAULT_CODE_BASE = "/groovy/shell";
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final Binding context;
    private final AtomicInteger counter = new AtomicInteger(0);
    private final CompilerConfiguration config;
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

    public GroovyShell(ClassLoader parent, CompilerConfiguration config) {
        this(parent, new Binding(), config);
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
        return run(scriptFile, (String[]) list.toArray(EMPTY_STRING_ARRAY));
    }

    /**
     * A helper method which runs the given cl script with the given command line arguments
     *
     * @param scriptText is the text content of the script
     * @param fileName   is the logical file name of the script (which is used to create the class name of the script)
     * @param list       the command line arguments to pass in
     */
    public Object run(String scriptText, String fileName, List list) throws CompilationFailedException {
        return run(scriptText, fileName, (String[]) list.toArray(EMPTY_STRING_ARRAY));
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
        // Always set the "args" property, regardless of what path we take in the code.
        // Bad enough to have side effects but worse if their behavior is wonky.
        context.setProperty("args", args);

        if (scriptClass == null) {
            return null;
        }

        //TODO: This logic mostly duplicates InvokerHelper.createScript.  They should probably be unified.

        if (Script.class.isAssignableFrom(scriptClass)) {
            // treat it just like a script if it is one
            try {
                Script script = InvokerHelper.newScript(scriptClass, context);
                return script.run();
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                // ignore instantiation errors,, try to do main
            }
        }
        try {
            // let's find a main method
            scriptClass.getMethod(MAIN_METHOD_NAME, String[].class);
            // if that main method exist, invoke it
            return InvokerHelper.invokeMethod(scriptClass, MAIN_METHOD_NAME, new Object[]{args});
        } catch (NoSuchMethodException e) {
            // if it implements Runnable, try to instantiate it
            if (Runnable.class.isAssignableFrom(scriptClass)) {
                return runRunnable(scriptClass, args);
            }
            GroovyRunnerRegistry runnerRegistry = GroovyRunnerRegistry.getInstance();
            for (GroovyRunner runner : runnerRegistry) {
                if (runner.canRun(scriptClass, this.loader)) {
                    return runner.run(scriptClass, this.loader);
                }
            }
            StringBuilder message = new StringBuilder("This script or class could not be run.\n" +
                    "It should either:\n" +
                    "- have a main method,\n" +
                    "- be a JUnit test or extend GroovyTestCase,\n" +
                    "- implement the Runnable interface,\n" +
                    "- or be compatible with a registered script runner. Known runners:\n");
            if (runnerRegistry.isEmpty()) {
                message.append("  * <none>");
            } else {
                for (String key : runnerRegistry.keySet()) {
                    message.append("  * ").append(key).append("\n");
                }
            }
            throw new GroovyRuntimeException(message.toString());
        }
    }

    private static Object runRunnable(Class scriptClass, String[] args) {
        Constructor constructor = null;
        Runnable runnable = null;
        Throwable reason = null;

        try {
            // first, fetch the constructor taking String[] as parameter
            constructor = scriptClass.getConstructor(String[].class);
            try {
                // instantiate a runnable and run it
                runnable = (Runnable) constructor.newInstance(new Object[]{args});
            } catch (Throwable t) {
                reason = t;
            }
        } catch (NoSuchMethodException e1) {
            try {
                // otherwise, find the default constructor
                constructor = scriptClass.getConstructor();
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
        return run(gcs, args);
    }

    /**
     * Runs the given script source with command line arguments
     *
     * @param source    is the source content of the script
     * @param args      the command line arguments to pass in
     */
    public Object run(GroovyCodeSource source, List args) throws CompilationFailedException {
        return run(source, ((String[]) args.toArray(EMPTY_STRING_ARRAY)));
    }

    /**
     * Runs the given script source with command line arguments
     *
     * @param source    is the source content of the script
     * @param args      the command line arguments to pass in
     */
    public Object run(GroovyCodeSource source, String[] args) throws CompilationFailedException {
        Class scriptClass = parseClass(source);
        return runScriptOrMainOrTestOrRunnable(scriptClass, args);
    }

    /**
     * Runs the given script source with command line arguments
     *
     * @param source    is the source content of the script
     * @param args      the command line arguments to pass in
     */
    public Object run(URI source, List args) throws CompilationFailedException, IOException {
        return run(new GroovyCodeSource(source), ((String[]) args.toArray(EMPTY_STRING_ARRAY)));
    }

    /**
     * Runs the given script source with command line arguments
     *
     * @param source    is the source content of the script
     * @param args      the command line arguments to pass in
     */
    public Object run(URI source, String[] args) throws CompilationFailedException, IOException {
        return run(new GroovyCodeSource(source), args);
    }

    /**
     * Runs the given script with command line arguments
     *
     * @param in       the stream reading the script
     * @param fileName is the logical file name of the script (which is used to create the class name of the script)
     * @param list     the command line arguments to pass in
     */
    public Object run(final Reader in, final String fileName, List list) throws CompilationFailedException {
        return run(in, fileName, (String[]) list.toArray(EMPTY_STRING_ARRAY));
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

    public void removeVariable(String name) {
        context.removeVariable(name);
    }

    /**
     * Evaluates some script against the current Binding and returns the result
     *
     * @param codeSource
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
     * @param uri is the URI of the script (which is used to create the class name of the script)
     */
    public Object evaluate(URI uri) throws CompilationFailedException, IOException {
        return evaluate(new GroovyCodeSource(uri));
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
     * @param uri is the URI of the script (which is used to create the class name of the script)
     */
    public Script parse(URI uri) throws CompilationFailedException, IOException {
        return parse(new GroovyCodeSource(uri));
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

    protected String generateScriptName() {
        return "Script" + counter.incrementAndGet() + ".groovy";
    }
}
