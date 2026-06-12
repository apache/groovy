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
package org.codehaus.groovy.tools;

/**
 * Holds the binary form of a compiled Groovy class.
 */
public class GroovyClass {
    /**
     * Shared empty array instance.
     */
    public static final GroovyClass[] EMPTY_ARRAY = {};

    private final String name;
    private final byte[] bytes;

    /**
     * Creates a compiled-class holder.
     *
     * @param name the binary class name
     * @param bytes the compiled class bytes
     */
    public GroovyClass(String name, byte[] bytes) {
        this.name  = name;
        this.bytes = bytes;
    }

    /**
     * Returns the binary class name.
     *
     * @return the class name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the compiled class bytes.
     *
     * @return the class bytes
     */
    public byte[] getBytes() {
        return this.bytes;
    }
}
