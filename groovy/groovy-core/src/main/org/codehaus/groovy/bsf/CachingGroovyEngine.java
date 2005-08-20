/*
 * $Id$
 * 
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
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

import java.io.ByteArrayInputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * A Caching implementation of the GroovyEngine
 *
 * @author James Birchfield
 */
public class CachingGroovyEngine extends GroovyEngine {
    private static final Object[] EMPTY_ARGS = new Object[]{new String[]{}};

    private Map evalScripts;
    private Map execScripts;
    private Binding context;
    private ClassLoader parent;
    private GroovyClassLoader loader;


    /**
     * Evaluate an expression.
     */
    public Object eval(String source, int lineNo, int columnNo, Object script) throws BSFException {
        try {
            //          Object result = shell.evaluate(script.toString(), source);
            Class scriptClass = (Class) evalScripts.get(script);
            if (scriptClass == null) {
                scriptClass = loader.parseClass(new ByteArrayInputStream(script.toString().getBytes()), source);
                evalScripts.put(script, scriptClass);
            } else {
                System.out.println("eval() - Using cached script...");
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

            Class scriptClass = (Class) execScripts.get(script);
            if (scriptClass == null) {
                scriptClass = loader.parseClass(new ByteArrayInputStream(script.toString().getBytes()), source);
                execScripts.put(script, scriptClass);
            } else {
                System.out.println("exec() - Using cached version of class...");
            }
            InvokerHelper.invokeMethod(scriptClass, "main", EMPTY_ARGS);
        } catch (Exception e) {
            System.err.println("BSF trace");
            e.printStackTrace(System.err);
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR, "exception from Groovy: " + e, e);
        }
    }

    /**
     * Initialize the engine.
     */
    public void initialize(final BSFManager mgr, String lang, Vector declaredBeans) throws BSFException {
        super.initialize(mgr, lang, declaredBeans);
        parent = mgr.getClassLoader();
        if (parent == null)
            parent = GroovyShell.class.getClassLoader();
        final ClassLoader finalParent = parent;
        this.loader =
                (GroovyClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        CompilerConfiguration configuration = new CompilerConfiguration();
                        configuration.setClasspath(mgr.getClassPath());
                        return new GroovyClassLoader(finalParent, configuration);
                    }
                });
        execScripts = new HashMap();
        evalScripts = new HashMap();
        context = shell.getContext();
        // create a shell

        // register the mgr with object name "bsf"
        context.setVariable("bsf", new BSFFunctions(mgr, this));

        int size = declaredBeans.size();
        for (int i = 0; i < size; i++) {
            declareBean((BSFDeclaredBean) declaredBeans.elementAt(i));
        }
    }
}
