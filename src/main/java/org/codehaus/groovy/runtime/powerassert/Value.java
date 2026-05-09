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
package org.codehaus.groovy.runtime.powerassert;

/**
 * A value recorded during evaluation of an assertion, along with the column it
 * is associated with in the assertion's normalized source text.
 */
public class Value {
    private final Object value;
    private final int column;

    /**
     * Creates a recorded assertion value.
     *
     * @param value the recorded value
     * @param column the 1-based column in the normalized assertion text, or a
     * non-positive value if the source position is unknown
     */
    public Value(Object value, int column) {
        this.value = value;
        this.column = column;
    }

    /**
     * Returns the recorded value.
     *
     * @return the recorded value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the source column associated with the recorded value.
     *
     * @return the 1-based column in the normalized assertion text, or a
     * non-positive value if the source position is unknown
     */
    public int getColumn() {
        return column;
    }
}
