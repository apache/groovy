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
package org.codehaus.groovy.classgen.asm.sc;

/**
 * Internal AST node metadata keys used by the statically-compiled lambda and method-reference pipeline.
 *
 * @since 6.0.0
 */
enum StaticTypesFunctionalInterfaceMetadataKey {
    /**
     * Stores the shared {@code $deserializeLambda$} guard block on the enclosing class node.
     */
    DESERIALIZE_LAMBDA_DISPATCHER,
    /**
     * Marks the synthetic constructor created for a generated lambda class.
     */
    LAMBDA_GENERATED_CONSTRUCTOR,
    /**
     * Stores the captured shared variables prepared for a lambda expression.
     */
    LAMBDA_SHARED_VARIABLES,
    /**
     * Marks the deserialize helper method that preloads a captured lambda receiver.
     */
    LAMBDA_PRELOADED_RECEIVER,
    /**
     * Caches whether the analyzed lambda method touches enclosing-instance state.
     */
    LAMBDA_ACCESSES_INSTANCE_MEMBERS,
    /**
     * Stores the synthetic deserialize helper name allocated for a method-reference expression so
     * repeated bytecode-generation visits can reuse the same helper slot.
     */
    METHOD_REFERENCE_DESERIALIZE_METHOD_NAME
}
