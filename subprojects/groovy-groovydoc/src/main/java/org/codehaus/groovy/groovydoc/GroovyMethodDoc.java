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
 * Describes a method in the Groovydoc model.
 */
public interface GroovyMethodDoc extends GroovyExecutableMemberDoc {
    /**
     * Indicates whether this method is declared abstract.
     *
     * @return {@code true} if this method is abstract
     */
    boolean isAbstract();
    /**
     * Returns the class in which the overridden method is declared.
     *
     * @return the overridden method's declaring class, or {@code null} if none exists
     */
    GroovyClassDoc overriddenClass();
    /**
     * Returns the method that this method overrides.
     *
     * @return the overridden method, or {@code null} if none exists
     */
    GroovyMethodDoc overriddenMethod();
    /**
     * Returns the type that declares the method overridden by this method.
     *
     * @return the overridden type, or {@code null} if none exists
     */
    GroovyType overriddenType();
    /**
     * Indicates whether this method overrides the supplied method.
     *
     * @param arg0 the candidate method
     * @return {@code true} if this method overrides {@code arg0}
     */
    boolean overrides(GroovyMethodDoc arg0);
    /**
     * Returns the declared return type of this method.
     *
     * @return the return type
     */
    GroovyType returnType();

    //---- additional
    /**
     * Updates the declared return type of this method.
     *
     * @param o the return type to associate with this method
     */
    void setReturnType(GroovyType o);
}
