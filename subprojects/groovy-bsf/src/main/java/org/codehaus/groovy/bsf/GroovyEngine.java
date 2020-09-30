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

import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import org.apache.bsf.BSFDeclaredBean;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.BSFEngineImpl;
import org.apache.bsf.util.BSFFunctions;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Vector;

/**
 * A BSF Engine for the <a href="http://groovy-lang.org/">Groovy</a> scripting language.
 * <p>
 * It's inspired from the Jython engine
 */
public class GroovyEngine extends BSFEngineImpl {
    protected GroovyShell shell;

    /*
     * Convert a non java class name to a java classname
     * This is used to convert a script name to a name
     * that can be used as a classname with the script is
     * loaded in GroovyClassloader#load()
     * The method simply replaces any invalid characters
     * with "_".
     */
    private static String convertToValidJavaClassname(String inName) {
        if (inName == null) return "_";
        if (inName.startsWith("scriptdef_")) inName = inName.substring(10);
        if (inName.isEmpty()) return "_";

        StringBuilder output = new StringBuilder(inName.length());
        boolean firstChar = true;
        for (int i = 0; i < inName.length(); ++i) {
            char ch = inName.charAt(i);
            if (firstChar && !Character.isJavaIdentifierStart(ch)) {
                ch = '_';
            } else if (!firstChar
                    && !(Character.isJavaIdentifierPart(ch) || ch == '.')) {
                ch = '_';
            }
            firstChar = (ch == '.');
            output.append(ch);
        }
        return output.toString();
    }

    /**
     * Allow an anonymous function to be declared and invoked
     */
    @Override
    public Object apply(String source, int lineNo, int columnNo, Object funcBody, Vector paramNames,
                        Vector arguments) throws BSFException {
        Object object = eval(source, lineNo, columnNo, funcBody);
        if (object instanceof Closure) {
            // lets call the function
            Closure closure = (Closure) object;
            return closure.call(arguments.toArray());
        }
        return object;
    }

    /**
     * Call the named method of the given object.
     */
    @Override
    public Object call(Object object, String method, Object[] args) throws BSFException {
        return InvokerHelper.invokeMethod(object, method, args);
    }

    /**
     * Evaluate an expression.
     */
    @Override
    public Object eval(String source, int lineNo, int columnNo, Object script) throws BSFException {
        try {
            source = convertToValidJavaClassname(source);
            return getEvalShell().evaluate(script.toString(), source);
        } catch (Exception e) {
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR, "exception from Groovy: " + e, e);
        }
    }

    /**
     * Execute a script.
     */
    @Override
    public void exec(String source, int lineNo, int columnNo, Object script) throws BSFException {
        try {
            // use evaluate to pass in the BSF variables
            source = convertToValidJavaClassname(source);
            getEvalShell().evaluate(script.toString(), source);
        } catch (Exception e) {
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR, "exception from Groovy: " + e, e);
        }
    }

    /**
     * Initialize the engine.
     */
    @Override
    public void initialize(BSFManager mgr, String lang, Vector declaredBeans) throws BSFException {
        super.initialize(mgr, lang, declaredBeans);

        // create a shell
        shell = new GroovyShell(mgr.getClassLoader());

        // register the mgr with object name "bsf"
        shell.setVariable("bsf", new BSFFunctions(mgr, this));

        int size = declaredBeans.size();
        for (int i = 0; i < size; i++) {
            declareBean((BSFDeclaredBean) declaredBeans.elementAt(i));
        }
    }

    /**
     * Declare a bean
     */
    @Override
    public void declareBean(BSFDeclaredBean bean) throws BSFException {
        shell.setVariable(bean.name, bean.bean);
    }

    /**
     * Undeclare a previously declared bean.
     */
    @Override
    public void undeclareBean(BSFDeclaredBean bean) throws BSFException {
        shell.setVariable(bean.name, null);
    }

    /**
     * @return a newly created GroovyShell using the same variable scope but a new class loader
     */
    protected GroovyShell getEvalShell() {
        return new GroovyShell(shell);
    }
}
