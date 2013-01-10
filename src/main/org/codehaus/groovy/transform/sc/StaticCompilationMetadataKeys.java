/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform.sc;

/**
 * Static compilation AST node metadata keys.
 *
 * @author Cedric Champeau
 */
public enum StaticCompilationMetadataKeys {
    STATIC_COMPILE_NODE, // used to mark a section of code as to be statically compiled
    BINARY_EXP_TARGET, // use to tell which method should be used in a binary expression
    PRIVATE_BRIDGE_METHODS, // private bridge methods are methods used by an outer class to access an inner class method
    PRIVATE_FIELDS_ACCESSORS, // private constants methods are methods used by an outer class to access an outer class constant
    PROPERTY_OWNER, // the type of the class which owns the property
    COMPONENT_TYPE // for list.property expressions, we need the inferred component type
}
