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
package org.codehaus.groovy.groovydoc;

/**
 * Describes a parameter declared by a documented executable member.
 */
public interface GroovyParameter {
    /**
     * Returns the annotations declared on this parameter.
     *
     * @return the parameter annotations
     */
    GroovyAnnotationRef[] annotations();

    /**
     * Returns the parameter name.
     *
     * @return the parameter name
     */
    String name();

    /**
     * Returns the resolved parameter type.
     *
     * @return the parameter type, or {@code null} if it is unavailable
     */
    GroovyType type();

    /**
     * Returns the parameter type name as declared in source.
     *
     * @return the declared type name
     */
    String typeName();

    /**
     * Returns the default value expression declared for this parameter.
     *
     * @return the default value expression, or {@code null} if none is declared
     */
    String defaultValue();
}
