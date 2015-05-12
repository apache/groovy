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
package org.codehaus.groovy.runtime.memoize

/**
 * @author Vaclav Pech
 */

public class MemoizeTest extends AbstractMemoizeTestCase {

    Closure buildMemoizeClosure(Closure cl) {
        cl.memoize()
    }

    void testMemoizeWithInject() {
        int maxExecutionCount = 0
        Closure max = { int a, int b ->
            maxExecutionCount++
            Math.max(a, b)
        }.memoize()
        int minExecutionCount = 0
        Closure min = { int a, int b ->
            minExecutionCount++
            Math.min(a, b)
        }.memoize()
        100.times {
            max.call(max.call(1, 2), 3)
        }
        100.times {
            [1, 2, 3].inject(min)
        }
        assert maxExecutionCount == 2
        assert minExecutionCount == 2
    }
}
