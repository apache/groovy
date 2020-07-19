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
package org.apache.groovy.contracts.ast.visitor;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;

/**
 * <p>
 * Base class for {@link org.codehaus.groovy.ast.ClassCodeVisitorSupport} descendants. This class is used in groovy-contracts
 * as root class for all code visitors directly used by global AST transformations.
 * </p>
 *
 * @see org.codehaus.groovy.ast.ClassCodeVisitorSupport
 */
public abstract class BaseVisitor extends ClassCodeVisitorSupport {

    public static final String GCONTRACTS_ENABLED_VAR = "$GCONTRACTS_ENABLED";

    public static final String CLOSURE_ATTRIBUTE_NAME = "value";

    protected SourceUnit sourceUnit;
    protected ReaderSource source;

    public BaseVisitor(final SourceUnit sourceUnit, final ReaderSource source) {
        this.sourceUnit = sourceUnit;
        this.source = source;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }
}
