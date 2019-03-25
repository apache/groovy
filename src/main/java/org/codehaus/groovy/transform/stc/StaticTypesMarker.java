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
package org.codehaus.groovy.transform.stc;

/**
 * This enumeration is used by the AST transformations which rely on static type checking, either
 * to store or to retrieve information from AST node metadata. The values of this enumeration are
 * used as metadata keys.
 */
public enum StaticTypesMarker {
    INFERRED_TYPE, // used to store type information on class nodes
    DECLARATION_INFERRED_TYPE, // in flow analysis, represents the type of the declaration node lhs
    INFERRED_RETURN_TYPE, // used to store inferred return type for methods and closures
    CLOSURE_ARGUMENTS, // used to store closure argument types on a variable expression
    READONLY_PROPERTY, // used to tell that a property expression refers to a readonly property
    INITIAL_EXPRESSION, // used to store the default expression for a parameter
    DIRECT_METHOD_CALL_TARGET, // used to store the MethodNode a MethodCallExpression should target
    DELEGATION_METADATA, // used to store the delegation strategy and delegate type of a closure when declared with @DelegatesTo
    IMPLICIT_RECEIVER, // if the receiver is implicit but not "this", store the name of the receiver (delegate or owner)
    PV_FIELDS_ACCESS, // set of private fields that are accessed from closures or inner classes
    PV_FIELDS_MUTATION, // set of private fields that are set from closures or inner classes
    PV_METHODS_ACCESS, // set of private methods that are accessed from closures or inner classes
    DYNAMIC_RESOLUTION, // call recognized by a type checking extension as a dynamic method call
    SUPER_MOP_METHOD_REQUIRED, // used to store the list of MOP methods that still have to be generated
    PARAMETER_TYPE, // used to store the parameter type information of method invocation on an expression
    INFERRED_FUNCTIONAL_INTERFACE_TYPE, // used to store the function interface type information on an expression
    CONSTRUCTED_LAMBDA_EXPRESSION // used to store the constructed lambda expression for method reference and constructor reference
}
