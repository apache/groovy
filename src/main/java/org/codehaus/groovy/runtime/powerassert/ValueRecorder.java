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

import java.util.ArrayList;
import java.util.List;

/**
 * Records values produced during evaluation of an assertion statement's truth
 * expression.
 */
public class ValueRecorder {
    private final List<Value> values = new ArrayList<Value>();

    /**
     * Removes all values recorded for the current assertion evaluation.
     */
    public void clear() {
        values.clear();
    }

    /**
     * Records a value at the supplied anchor column and returns it unchanged.
     *
     * @param value the value produced while evaluating the assertion
     * @param anchor the 1-based column in the normalized assertion text, or a
     * non-positive value if the position is unknown
     * @return {@code value}
     */
    public Object record(Object value, int anchor) {
        values.add(new Value(value, anchor));
        return value;
    }

    /**
     * Returns the recorded values in recording order.
     *
     * @return the live list of recorded values
     */
    public List<Value> getValues() {
        return values;
    }
}
