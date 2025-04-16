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
package groovy.transform.stc.vm6

import groovy.transform.stc.StaticTypeCheckingTestCase

/**
 * Unit tests for static type checking : method calls.
 */
class MethodCallsSTCTest extends StaticTypeCheckingTestCase {
    // GROOVY-5525
    void testFindMethodFromArraysClass() {
        assertScript '''import groovy.transform.TypeChecked
        import java.util.Arrays

        class ArrayCopying {
            public static void main(String[] args) {
                def acopy = Arrays.copyOf(args, 1)
            }

        }
        '''
    }
}
