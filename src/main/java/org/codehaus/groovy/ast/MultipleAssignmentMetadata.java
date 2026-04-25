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
 * AST node metadata keys used by the multi-assignment destructuring pipeline
 * introduced in GEP-20. Each key is attached to a {@code VariableExpression}
 * appearing inside the {@code TupleExpression} on the LHS of a
 * {@code DeclarationExpression}.
 */
public enum MultipleAssignmentMetadata {
    /** Marker on the rest binder (the {@code *ident} slot). Value is {@link Boolean#TRUE}. */
    REST_BINDING,
    /** Value is the key name (String) used for a map-style {@code key: ident} binder. */
    MAP_KEY
}
