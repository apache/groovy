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
package gls.scope;

import gls.CompilableTestSupport

class ClassVariableHidingTest extends CompilableTestSupport {

    def foo = 1;
    def bar = 2;

    void testFooHiding() {
        assert foo == 1
        def foo = 5
        assert foo == 5
    }

    void testBarHiding() {
        assert bar == 2
        def bar = 5
        assert bar == 5
    }
}