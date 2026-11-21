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

/**
 * Represents a package in the AST.
 */
public class PackageNode extends AnnotatedNode {
    private final String name;

    /**
     * Creates a package node for {@code name}.
     *
     * @param name the package name, as written in the source
     */
    public PackageNode(String name) {
        this.name = name;
    }

    /**
     * The package name, without the {@code package} keyword.
     *
     * @return the name passed to the constructor
     */
    public String getName() {
        return name;
    }

    /**
     * @return the text display of this package definition
     */
    @Override
    public String getText() {
        return "package " + name;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
    }
}
