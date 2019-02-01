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
package org.codehaus.groovy.classgen.asm;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;

/**
 * Interface for modules which are capable of resolving the type of an expression.
 * Several implementations are available, depending on whether you are in a dynamic
 * or static compilation mode.
 */
public interface TypeChooser {

    /**
     * Resolve the type of an expression. Depending on the implementations, the
     * returned type may be the declared type or an inferred type.
     * @param expression the expression for which the type must be returned.
     * @param classNode the classnode this expression belongs to
     * @return the resolved type.
     */
    ClassNode resolveType(final Expression expression, ClassNode classNode);

}
