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
package groovy.bugs

import groovy.test.GroovyTestCase
import org.codehaus.groovy.runtime.MethodClosure

class Groovy4104Bug extends GroovyTestCase {
    void testMethodClosureWithProtectedMethodInSuperClass() {
        MethodClosure mc1 = new Groovy4104A().createMethodClosure();
        MethodClosure mc2 = new Groovy4104B().createMethodClosure();

        assert mc1.call(11) == 22
        assert mc1.getMaximumNumberOfParameters() == 1

        assert mc2.call(22) == 44
        assert mc2.getMaximumNumberOfParameters() == 1
    }
}