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
package org.apache.groovy.contracts.generation

import org.junit.Test

class ContractExecutionTrackerTests {

    @Test
    void track_double_execution() {

        ContractExecutionTracker.clear('Dummy', 'method 1', 'pre', false)

        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre', false)
        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre', false) == false

        ContractExecutionTracker.clear('Dummy', 'method 1', 'pre', false)
    }

    @Test
    void clear_only_for_first_stack_element() {

        ContractExecutionTracker.clear('Dummy', 'method 1', 'pre', false)
        ContractExecutionTracker.clear('Dummy', 'method 2', 'pre', false)

        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre', false)
        assert ContractExecutionTracker.track('Dummy', 'method 2', 'pre', false)
        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre', false) == false

        ContractExecutionTracker.clear('Dummy', 'method 2', 'pre', false)

        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre', false) == false
        ContractExecutionTracker.clear('Dummy', 'method 1', 'pre', false)
        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre', false)
    }

    @Test
    void track_static_method() {

        ContractExecutionTracker.clear('Dummy', 'method 1', 'pre', false)

        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre', true)
        assert ContractExecutionTracker.track('Dummy', 'method 1', 'pre', true) == false

        ContractExecutionTracker.clear('Dummy', 'method 1', 'pre', true)
    }

}
