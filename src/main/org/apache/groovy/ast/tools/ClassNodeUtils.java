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
package org.apache.groovy.ast.tools;

import org.codehaus.groovy.ast.ClassNode;

/**
 * Utility class for working with ClassNodes
 */
public class ClassNodeUtils {
    /**
     * Formats a type name into a human readable version. For arrays, appends "[]" to the formatted
     * type name of the component. For unit class nodes, uses the class node name.
     *
     * @param cNode the type to format
     * @return a human readable version of the type name (java.lang.String[] for example)
     */
    public static String formatTypeName(ClassNode cNode) {
        if (cNode.isArray()) {
            ClassNode it = cNode;
            int dim = 0;
            while (it.isArray()) {
                dim++;
                it = it.getComponentType();
            }
            StringBuilder sb = new StringBuilder(it.getName().length() + 2 * dim);
            sb.append(it.getName());
            for (int i = 0; i < dim; i++) {
                sb.append("[]");
            }
            return sb.toString();
        }
        return cNode.getName();
    }

    private ClassNodeUtils() { }
}
