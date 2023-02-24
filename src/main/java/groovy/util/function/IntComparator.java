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
package groovy.util.function;

/**
 * A comparator of two int values.
 */
@FunctionalInterface
public interface IntComparator {
    /**
     * Compares its two arguments for order.
     *
     * @param v1 The int value to compare.
     * @param v2 The int value to compare.
     * @return If v1 is less than v2, returns negative. If v1 equals to v2, returns zero. If v1 is greater than v2, returns positive.
     */
    int compare(int v1, int v2);
}
