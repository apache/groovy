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
package org.codehaus.groovy;

/**
 * An interface for use by all Groovy compiler exceptions.
 * Provides control over exception fatality, indicating whether an exception should halt compilation.
 */
public interface GroovyExceptionInterface {

    /**
     * Returns whether this exception is fatal.
     * A fatal exception typically causes compilation to stop.
     *
     * @return {@code true} if this exception is fatal; {@code false} otherwise
     */
    boolean isFatal();

    /**
     * Sets whether this exception is fatal.
     * A fatal exception typically causes compilation to stop.
     *
     * @param fatal {@code true} to mark this exception as fatal; {@code false} otherwise
     */
    void setFatal(boolean fatal);

}
