/*
 * Copyright 2003-2007 the original author or authors.
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

package groovy.util;

import org.codehaus.groovy.control.CompilationFailedException;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 * Allow easy integration from Groovy into Java through convenience methods.
 * 
 * @author Dierk Koenig
 */

public class Eval {
    /**
     * @param expression the Groovy expression to evaluate
     * @return the result of the expression
     * @throws CompilationFailedException if expression is no proper Groovy
     */
    public static Object me(final String expression) throws CompilationFailedException {
        return me(null, null, expression);
    }

    /**
     * evaluate expression and make object available inside the expression as 'symbol'
     * @param expression the Groovy expression to evaluate
     * @return the result of the expression
     * @throws CompilationFailedException if expression is no proper Groovy
     */
    public static Object me(final String symbol, final Object object, final String expression) throws CompilationFailedException {
        Binding b = new Binding();
        b.setVariable(symbol, object);
        GroovyShell sh = new GroovyShell(b);
        return sh.evaluate(expression);
    }

    /**
     * evaluate expression and make x available inside the expression as 'x'
     * @param expression the Groovy expression to evaluate
     * @return the result of the expression
     * @throws CompilationFailedException if expression is no proper Groovy
     */
    public static Object x(final Object x, final String expression) throws CompilationFailedException {
        return me("x", x, expression);
    }

    /**
     * evaluate expression and make x and y available inside the expression as 'x' and 'y'
     * @param expression the Groovy expression to evaluate
     * @return the result of the expression
     * @throws CompilationFailedException if expression is no proper Groovy
     */
    public static Object xy(final Object x, final Object y, final String expression) throws CompilationFailedException {
        Binding b = new Binding();
        b.setVariable("x", x);
        b.setVariable("y", y);
        GroovyShell sh = new GroovyShell(b);
        return sh.evaluate(expression);
    }

    /**
     * evaluate expression and make x,y,z available inside the expression as 'x','y','z'
     * @param expression the Groovy expression to evaluate
     * @return the result of the expression
     * @throws CompilationFailedException if expression is no proper Groovy
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
