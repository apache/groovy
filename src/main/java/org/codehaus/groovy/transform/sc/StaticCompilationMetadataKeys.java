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
package org.codehaus.groovy.transform.sc;

/**
 * Static compilation AST node metadata keys.
 */
public enum StaticCompilationMetadataKeys {
    /** Marks a section of code for static compilation. */
    STATIC_COMPILE_NODE,
    /** Tells which method should be used in a binary expression. */
    BINARY_EXP_TARGET,
    /** Private bridge methods are methods used to access a nestmate's method. */
    PRIVATE_BRIDGE_METHODS,
    /** Private fields accessors are methods used to read a nestmate's field. */
    @Deprecated(since = "5.0.0")
    PRIVATE_FIELDS_ACCESSORS,
    /** Private fields mutators are methods used to write a nestmate's field. */
    @Deprecated(since = "5.0.0")
    PRIVATE_FIELDS_MUTATORS,
    /** Callback for dynamic classes that contain statically compiled inner classes or methods. */
    DYNAMIC_OUTER_NODE_CALLBACK,
    /** The type of the class which owns the property. */
    PROPERTY_OWNER,
    /** For list.property expressions, we need the inferred component type. */
    COMPONENT_TYPE,
    /** If a receiver is the receiver of a dynamic property (for mixed-mode compilation). */
    RECEIVER_OF_DYNAMIC_PROPERTY
}
