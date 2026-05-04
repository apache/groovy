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
package org.codehaus.groovy.runtime;

/**
 * Holds information about a range, including its boundaries and direction.
 * Used internally by Groovy to represent range iteration parameters.
 */
public class RangeInfo {
    /**
     * The starting index of the range (inclusive).
     */
    public final int from;
    /**
     * The ending index of the range (inclusive).
     */
    public final int to;
    /**
     * Whether the range should be iterated in reverse order.
     */
    public final boolean reverse;

    /**
     * Constructs a RangeInfo with the specified boundaries and direction.
     *
     * @param from the starting index (inclusive)
     * @param to the ending index (inclusive)
     * @param reverse whether to iterate in reverse order
     */
    public RangeInfo(int from, int to, boolean reverse) {
        this.from = from;
        this.to = to;
        this.reverse = reverse;
    }
}
