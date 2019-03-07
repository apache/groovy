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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilationFailedException;

/**
 * Allow easy integration from Groovy into Java through convenience methods.
 * <p>
 * This class is a simple helper on top of GroovyShell. You can use it to evaluate small
 * Groovy scripts that don't need large Binding objects. For example, this script 
 * executes with no errors: 
 * <pre class="groovyTestCase">
 * assert Eval.me(' 2 * 4 + 2') == 10
 * assert Eval.x(2, ' x * 4 + 2') == 10
 * </pre>
 * 
 * @see GroovyShell
 */

public class Eval {
    /**
     * Evaluates the specified String expression and returns the result. For example: 
     * <pre class="groovyTestCase">
     * assert Eval.me(' 2 * 4 + 2') == 10
     * </pre>
     * @param expression the Groovy expression to evaluate
     * @return the result of the expression
     * @throws CompilationFailedException if expression is not valid Groovy
     */
    public static Object me(final String expression) throws CompilationFailedException {
        return me(null, null, expression);
    }

    /**
     * Evaluates the specified String expression and makes the parameter available inside
     * the script, returning the result. For example, this code binds the 'x' variable: 
     * <pre class="groovyTestCase">
     * assert Eval.me('x', 2, ' x * 4 + 2') == 10
     * </pre>
     * @param expression the Groovy expression to evaluate
     * @return the result of the expression
     * @throws CompilationFailedException if expression is not valid Groovy
     */
    public static Object me(final String symbol, final Object object, final String expression) throws CompilationFailedException {
        Binding b = new Binding();
        b.setVariable(symbol, object);
        GroovyShell sh = new GroovyShell(b);
        return sh.evaluate(expression);
    }

    /**
     * Evaluates the specified String expression and makes the parameter available inside
     * the script bound to a variable named 'x', returning the result. For example, this 
     * code executes without failure: 
     * <pre class="groovyTestCase">
     * assert Eval.x(2, ' x * 4 + 2') == 10
     * </pre>
     * @param expression the Groovy expression to evaluate
     * @return the result of the expression
     * @throws CompilationFailedException if expression is not valid Groovy
     */
    public static Object x(final Object x, final String expression) throws CompilationFailedException {
        return me("x", x, expression);
    }

    /**
     * Evaluates the specified String expression and makes the first two parameters available inside
     * the script bound to variables named 'x' and 'y' respectively, returning the result. For example, 
     * this code executes without failure: 
     * <pre class="groovyTestCase">
     * assert Eval.xy(2, 4, ' x * y + 2') == 10
     * </pre>
     * @param expression the Groovy expression to evaluate
     * @return the result of the expression
     * @throws CompilationFailedException if expression is not valid Groovy
     */
    public static Object xy(final Object x, final Object y, final String expression) throws CompilationFailedException {
        Binding b = new Binding();
        b.setVariable("x", x);
        b.setVariable("y", y);
        GroovyShell sh = new GroovyShell(b);
        return sh.evaluate(expression);
    }

    /**
     * Evaluates the specified String expression and makes the first three parameters available inside
     * the script bound to variables named 'x', 'y', and 'z' respectively, returning the result. For 
     * example, this code executes without failure: 
     * <pre class="groovyTestCase">
     * assert Eval.xyz(2, 4, 2, ' x * y + z') == 10
     * </pre>
     * @param expression the Groovy expression to evaluate
     * @return the result of the expression
     * @throws CompilationFailedException if expression is not valid Groovy
     */
    public static Object xyz(final Object x, final Object y, final Object z, final String expression) throws CompilationFailedException {
        Binding b = new Binding();
        b.setVariable("x", x);
        b.setVariable("y", y);
        b.setVariable("z", z);
        GroovyShell sh = new GroovyShell(b);
        return sh.evaluate(expression);
    }
}
