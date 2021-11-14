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
package org.codehaus.groovy.ast;

import java.lang.reflect.Modifier;
import java.util.StringJoiner;

/**
 * Helper class for converting AST into text.
 */
public class AstToTextHelper {

    public static String getClassText(final ClassNode node) {
        if (node == null) return "<unknown>";
        return node.toString(false);
    }

    public static String getParameterText(final Parameter node) {
        if (node == null) return "<unknown>";

        String name = node.getName();
        if (name == null) name = "<unknown>";
        String type = getClassText(node.getType());

        String text = type + " " + name;
        if (node.hasInitialExpression()) {
            text += " = " + node.getInitialExpression().getText();
        }
        return text;
    }

    public static String getParametersText(final Parameter[] parameters) {
        if (parameters == null || parameters.length == 0) return "";
        StringJoiner result = new StringJoiner(", ");
        for (Parameter parameter : parameters) {
            result.add(getParameterText(parameter));
        }
        return result.toString();
    }

    public static String getThrowsClauseText(final ClassNode[] exceptions) {
        if (exceptions == null || exceptions.length == 0) return "";
        StringJoiner result = new StringJoiner(", ");
        for (ClassNode exception : exceptions) {
            result.add(getClassText(exception));
        }
        return " throws " + result.toString();
    }

    public static String getModifiersText(final int modifiers) {
        StringJoiner result = new StringJoiner(" ");
        if (Modifier.isPrivate(modifiers)) {
            result.add("private");
        }
        if (Modifier.isProtected(modifiers)) {
            result.add("protected");
        }
        if (Modifier.isPublic(modifiers)) {
            result.add("public");
        }
        if (Modifier.isStatic(modifiers)) {
            result.add("static");
        }
        if (Modifier.isAbstract(modifiers)) {
            result.add("abstract");
        }
        if (Modifier.isFinal(modifiers)) {
            result.add("final");
        }
        if (Modifier.isInterface(modifiers)) {
            result.add("interface");
        }
        if (Modifier.isNative(modifiers)) {
            result.add("native");
        }
        if (Modifier.isSynchronized(modifiers)) {
            result.add("synchronized");
        }
        if (Modifier.isTransient(modifiers)) {
            result.add("transient");
        }
        if (Modifier.isVolatile(modifiers)) {
            result.add("volatile");
        }
        return result.toString();
    }
}
