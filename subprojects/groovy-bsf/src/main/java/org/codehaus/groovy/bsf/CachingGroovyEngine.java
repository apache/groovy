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
package org.codehaus.groovy.bsf;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.bsf.BSFDeclaredBean;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.BSFFunctions;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Caching implementation of the GroovyEngine
 */
public class CachingGroovyEngine extends GroovyEngine {
    private static final Logger LOG = Logger.getLogger(CachingGroovyEngine.class.getName());
    private static final Object[] EMPTY_ARGS = new Object[]{new String[]{}};

    private Map<Object, Class> evalScripts;
    private Map<Object, Class> execScripts;
    private Binding context;
    private GroovyClassLoader loader;

    /**
     * Evaluate an expression.
     */
    public Object eval(String source, int lineNo, int columnNo, Object script) throws BSFException {
        try {
            Class scriptClass = evalScripts.get(script);
            if (scriptClass == null) {
                scriptClass = loader.parseClass(script.toString(), source);
                evalScripts.put(script, scriptClass);
            } else {
                LOG.fine("eval() - Using cached script...");
            }
            //can't cache the script because the context may be different.
            //but don't bother loading parsing the class again
            Script s = InvokerHelper.createScript(scriptClass, context);
            return s.run();
        } catch (Exception e) {
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR, "exception from Groovy: " + e, e);
        }
    }

    /**
     * Execute a script.
     */
    public void exec(String source, int lineNo, int columnNo, Object script) throws BSFException {
        try {
            //          shell.run(script.toString(), source, EMPTY_ARGS);

            Class scriptClass = execScripts.get(script);
            if (scriptClass == null) {
                scriptClass = loader.parseClass(script.toString(), source);
                execScripts.put(script, scriptClass);
            } else {
                LOG.fine("exec() - Using cached version of class...");
            }
            InvokerHelper.invokeMethod(scriptClass, "main", EMPTY_ARGS);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "BSF trace", e);
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR, "exception from Groovy: " + e, e);
        }
    }

    /**
     * Initialize the engine.
     */
    public void initialize(final BSFManager mgr, String lang, Vector declaredBeans) throws BSFException {
        super.initialize(mgr, lang, declaredBeans);
        ClassLoader parent = mgr.getClassLoader();
        if (parent == null)
            parent = GroovyShell.class.getClassLoader();
        setLoader(mgr, parent);
        execScripts = new HashMap<>();
        evalScripts = new HashMap<>();
        context = shell.getContext();
        // create a shell
        // register the mgr with object name "bsf"
        context.setVariable("bsf", new BSFFunctions(mgr, this));
        int size = declaredBeans.size();
        for (int i = 0; i < size; i++) {
            declareBean((BSFDeclaredBean) declaredBeans.elementAt(i));
        }
    }

    @SuppressWarnings("unchecked")
    private void setLoader(final BSFManager mgr, final ClassLoader finalParent) {
        this.loader =
                (GroovyClassLoader) AccessController.doPrivileged((PrivilegedAction) () -> {
                    CompilerConfiguration configuration = new CompilerConfiguration();
                    configuration.setClasspath(mgr.getClassPath());
                    return new GroovyClassLoader(finalParent, configuration);
                });
    }
}
