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
 * Describes an executable member such as a method or constructor.
 */
public interface GroovyExecutableMemberDoc extends GroovyMemberDoc {
    /**
     * Returns the flat signature used in rendered output.
     *
     * @return the flat signature
     */
    String flatSignature();
    /**
     * Indicates whether this executable is declared {@code native}.
     *
     * @return {@code true} if this executable is native
     */
    boolean isNative();
    /**
     * Indicates whether this executable is declared {@code synchronized}.
     *
     * @return {@code true} if this executable is synchronized
     */
    boolean isSynchronized();
    /**
     * Indicates whether the executable accepts a variable number of arguments.
     *
     * @return {@code true} if this executable is variadic
     */
    boolean isVarArgs();
    /**
     * Returns the parameters declared by this executable.
     *
     * @return the declared parameters
     */
    GroovyParameter[] parameters();
    /**
     * Returns the full signature used in rendered output.
     *
     * @return the executable signature
     */
    String signature();
    /**
     * Returns the checked exceptions declared by this executable.
     *
     * @return the declared exceptions
     */
    GroovyClassDoc[] thrownExceptions();
    /**
     * Returns the checked exception types declared by this executable.
     *
     * @return the declared exception types
     */
    GroovyType[] thrownExceptionTypes();
}
