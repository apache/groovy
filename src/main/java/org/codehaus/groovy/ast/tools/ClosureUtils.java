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
package org.codehaus.groovy.ast.tools;

import groovy.lang.Closure;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.control.io.ReaderSource;
/**
 * Handy methods when working with Closure AST data structures.
 */

public class ClosureUtils {

    /**
     * Converts a ClosureExpression into the String source.
     *
     * @param readerSource a source
     * @param expression a closure. Can't be null
     * @return the source the closure was created from
     * @throws java.lang.IllegalArgumentException when expression is null
     * @throws java.lang.Exception when closure can't be read from source
     */
    public static String convertClosureToSource(ReaderSource readerSource, ClosureExpression expression) throws Exception {
        String source = GeneralUtils.convertASTToSource(readerSource, expression);
        if (!source.startsWith("{")) {
            throw new Exception("Error converting ClosureExpression into source code. Closures must start with {. Found: " + source);
        }
        return source;
    }

    /**
     * Does the Closure have a single char-like (char or Character) argument.
     * @param c a Closure
     * @return true if it has exactly one argument and the type is char or Character
     */
    public static boolean hasSingleCharacterArg(Closure c) {
        if (c.getMaximumNumberOfParameters() != 1) return false;
        String typeName = c.getParameterTypes()[0].getName();
        return typeName.equals("char") || typeName.equals("java.lang.Character");
    }

    /**
     * Does the Closure have a single String argument.
     * @param c a Closure
     * @return true if it has exactly one argument and the type is String
     */
    public static boolean hasSingleStringArg(Closure c) {
        if (c.getMaximumNumberOfParameters() != 1) return false;
        String typeName = c.getParameterTypes()[0].getName();
        return typeName.equals("java.lang.String");
    }

    /**
     * @return true if the ClosureExpression has an implicit 'it' parameter
     */
    public static boolean hasImplicitParameter(ClosureExpression ce) {
        return ce.getParameters() != null && ce.getParameters().length == 0;
    }

    /**
     * @return the parameters for the ClosureExpression
     */
    public static Parameter[] getParametersSafe(ClosureExpression ce) {
        return ce.getParameters() != null ? ce.getParameters() : Parameter.EMPTY_ARRAY;
    }

}
