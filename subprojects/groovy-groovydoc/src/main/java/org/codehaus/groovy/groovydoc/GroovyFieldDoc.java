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
 * Describes a field or property in the Groovydoc model.
 */
public interface GroovyFieldDoc extends GroovyMemberDoc {
    /**
     * Returns the constant value of this field when one is available.
     *
     * @return the constant value, or {@code null} if none is defined
     */
    Object constantValue();
    /**
     * Returns the source expression used to define the constant value.
     *
     * @return the constant value expression, or {@code null} if none is available
     */
    String constantValueExpression();
    /**
     * Indicates whether this field is declared {@code transient}.
     *
     * @return {@code true} if this field is transient
     */
    boolean isTransient();
    /**
     * Indicates whether this field is declared {@code volatile}.
     *
     * @return {@code true} if this field is volatile
     */
    boolean isVolatile();
    /**
     * Returns the declared type of this field.
     *
     * @return the field type
     */
    GroovyType type();
}
