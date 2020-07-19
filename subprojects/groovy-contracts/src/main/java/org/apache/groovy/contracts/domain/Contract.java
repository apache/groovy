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
package org.apache.groovy.contracts.domain;

import org.apache.groovy.contracts.util.Validate;
import org.codehaus.groovy.ast.ClassNode;

/**
 * <p>Represents a contract between a supplier and a customer of a class.</p>
 */
public class Contract {

    private final ClassNode classNode;

    private ClassInvariant classInvariant = ClassInvariant.DEFAULT;
    private final AssertionMap<Precondition> preconditions;
    private final AssertionMap<Postcondition> postconditions;

    public Contract(final ClassNode classNode) {
        Validate.notNull(classNode);

        this.classNode = classNode;
        this.preconditions = new AssertionMap<>();
        this.postconditions = new AssertionMap<>();
    }

    public ClassNode classNode() {
        return classNode;
    }

    public void setClassInvariant(final ClassInvariant classInvariant) {
        Validate.notNull(classInvariant);
        this.classInvariant = classInvariant;
    }

    public AssertionMap<Precondition> preconditions() {
        return preconditions;
    }

    public AssertionMap<Postcondition> postconditions() {
        return postconditions;
    }

    public boolean hasDefaultClassInvariant() {
        return classInvariant == ClassInvariant.DEFAULT;
    }

    public ClassInvariant classInvariant() {
        return classInvariant;
    }
}
