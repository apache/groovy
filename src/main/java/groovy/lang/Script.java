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

import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * This object represents a Groovy script
 */
public abstract class Script extends GroovyObjectSupport {
    private Binding binding;

    protected Script() {
        this(new Binding());
    }

    protected Script(Binding binding) {
        this.binding = binding;
    }

    public Binding getBinding() {
        return binding;
    }

    public void setBinding(Binding binding) {
        this.binding = binding;
    }

    @Override
    public Object getProperty(String property) {
        try {
            return binding.getVariable(property);
        } catch (MissingPropertyException e) {
            return super.getProperty(property);
        }
    }

    @Override
    public void setProperty(String property, Object newValue) {
        if ("binding".equals(property)) {
            setBinding((Binding) newValue);
        } else if ("metaClass".equals(property)) {
            setMetaClass((MetaClass) newValue);
        } else if (!binding.hasVariable(property)
                // GROOVY-9554: @Field adds setter
                && hasSetterMethodFor(property)) {
            super.setProperty(property, newValue);
        } else {
            binding.setVariable(property, newValue);
        }
    }

    private boolean hasSetterMethodFor(String property) {
        String setterName = GeneralUtils.getSetterName(property);
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.getParameterCount() == 1
                    // TODO: Test modifiers or return type?
                    && method.getName().equals(setterName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Invoke a method (or closure in the binding) defined.
     *
     * @param name method to call
     * @param args arguments to pass to the method
     * @return value
     */
    @Override
    public Object invokeMethod(String name, Object args) {
        try {
            return super.invokeMethod(name, args);
        }
        // if the method was not found in the current scope (the script's methods)
        // let's try to see if there's a method closure with the same name in the binding
        catch (MissingMethodException mme) {
            try {
                if (name.equals(mme.getMethod())) {
                    Object boundClosure = getProperty(name);
                    if (boundClosure instanceof Closure) {
                        return ((Closure) boundClosure).call((Object[])args);
                    } else {
                        throw mme;
                    }
                } else {
                    throw mme;
                }
            } catch (MissingPropertyException mpe) {
                throw mme;
            }
        }
    }

    /**
     * The main instance method of a script which has variables in scope
     * as defined by the current {@link Binding} instance.
     */
    public abstract Object run();

    // println helper methods

    /**
     * Prints a newline to the current 'out' variable which should be a PrintWriter
     * or at least have a println() method defined on it.
     * If there is no 'out' property then print to standard out.
     */
    public void println() {
        Object object;

        try {
            object = getProperty("out");
        } catch (MissingPropertyException e) {
            System.out.println();
            return;
        }

        InvokerHelper.invokeMethod(object, "println", ArgumentListExpression.EMPTY_ARRAY);
    }

    /**
     * Prints the value to the current 'out' variable which should be a PrintWriter
     * or at least have a print() method defined on it.
     * If there is no 'out' property then print to standard out.
     */
    public void print(Object value) {
        Object object;

        try {
            object = getProperty("out");
        } catch (MissingPropertyException e) {
            DefaultGroovyMethods.print(System.out,value);
            return;
        }

        InvokerHelper.invokeMethod(object, "print", new Object[]{value});
    }

    /**
     * Prints the value and a newline to the current 'out' variable which should be a PrintWriter
     * or at least have a println() method defined on it.
     * If there is no 'out' property then print to standard out.
     */
    public void println(Object value) {
        Object object;

        try {
            object = getProperty("out");
        } catch (MissingPropertyException e) {
            DefaultGroovyMethods.println(System.out,value);
            return;
        }

        InvokerHelper.invokeMethod(object, "println", new Object[]{value});
    }

    /**
     * Prints a formatted string using the specified format string and argument.
     *
     * @param format the format to follow
     * @param value the value to be formatted
     */
    public void printf(String format, Object value) {
        Object object;

        try {
            object = getProperty("out");
        } catch (MissingPropertyException e) {
            DefaultGroovyMethods.printf(System.out, format, value);
            return;
        }

        InvokerHelper.invokeMethod(object, "printf", new Object[] { format, value });
    }

    /**
     * Prints a formatted string using the specified format string and arguments.
     *
     * @param format the format to follow
     * @param values an array of values to be formatted
     */
    public void printf(String format, Object[] values) {
        Object object;

        try {
            object = getProperty("out");
        } catch (MissingPropertyException e) {
            DefaultGroovyMethods.printf(System.out, format, values);
            return;
        }

        InvokerHelper.invokeMethod(object, "printf", new Object[] { format, values });
    }

    /**
     * A helper method to allow the dynamic evaluation of groovy expressions using this
     * scripts binding as the variable scope
     *
     * @param expression is the Groovy script expression to evaluate
     */
    public Object evaluate(String expression) throws CompilationFailedException {
        GroovyShell shell = new GroovyShell(getClass().getClassLoader(), binding);
        return shell.evaluate(expression);
    }

    /**
     * A helper method to allow the dynamic evaluation of groovy expressions using this
     * scripts binding as the variable scope
     *
     * @param file is the Groovy script to evaluate
     */
    public Object evaluate(File file) throws CompilationFailedException, IOException {
        GroovyShell shell = new GroovyShell(getClass().getClassLoader(), binding);
        return shell.evaluate(file);
    }

    /**
     * A helper method to allow scripts to be run taking command line arguments
     */
    public void run(File file, String[] arguments) throws CompilationFailedException, IOException {
        GroovyShell shell = new GroovyShell(getClass().getClassLoader(), binding);
        shell.run(file, arguments);
    }
}
