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

/**
 * Helper class for converting AST into text.
 */
public class AstToTextHelper {

    public static String getClassText(ClassNode node) {
        if (node == null) return "<unknown>";
        if (node.getName() == null) return "<unknown>";
        return node.getName();
    }

    public static String getParameterText(Parameter node) {
        if (node == null) return "<unknown>";

        String name = node.getName() == null ? "<unknown>" : node.getName();
        String type = getClassText(node.getType());
        if (node.getInitialExpression() != null) {
            return type + " " + name + " = " + node.getInitialExpression().getText();
        }
        return type + " " + name;
    }

    public static String getParametersText(Parameter[] parameters) {
        if (parameters == null) return "";
        if (parameters.length == 0) return "";
        StringBuilder result = new StringBuilder();
        int max = parameters.length;
        for (int x = 0; x < max; x++) {
            result.append(getParameterText(parameters[x]));
            if (x < (max - 1)) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    public static String getThrowsClauseText(ClassNode[] exceptions) {
        if (exceptions == null) return "";
        if (exceptions.length == 0) return "";
        StringBuilder result = new StringBuilder("throws ");
        int max = exceptions.length;
        for (int x = 0; x < max; x++) {
            result.append(getClassText(exceptions[x]));
            if (x < (max - 1)) {
                result.append(", "); 
            }
        }
        return result.toString(); 
    }

    public static String getModifiersText(int modifiers) {
        StringBuilder result = new StringBuilder();
        if (Modifier.isPrivate(modifiers)) {
            result.append("private ");
        }
        if (Modifier.isProtected(modifiers)) {
            result.append("protected ");
        }
        if (Modifier.isPublic(modifiers)) {
            result.append("public ");
        }
        if (Modifier.isStatic(modifiers)) {
            result.append("static ");
        }
        if (Modifier.isAbstract(modifiers)) {
            result.append("abstract ");
        }
        if (Modifier.isFinal(modifiers)) {
            result.append("final ");
        }
        if (Modifier.isInterface(modifiers)) {
            result.append("interface ");
        }
        if (Modifier.isNative(modifiers)) {
            result.append("native ");
        }
        if (Modifier.isSynchronized(modifiers)) {
            result.append("synchronized ");
        }
        if (Modifier.isTransient(modifiers)) {
            result.append("transient ");
        }
        if (Modifier.isVolatile(modifiers)) {
            result.append("volatile ");
        }
        return result.toString().trim();
    }
}