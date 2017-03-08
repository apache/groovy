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
package groovy

import org.codehaus.groovy.control.MultipleCompilationErrorsException

class EqualsTest extends GroovyShellTestCase {

    void testParentChildrenEquals() {
        def x = new Date()
        def y = new java.sql.Time(x.time)
        // Gotcha: don't try this with Timestamp,
        // see: http://mattfleming.com/node/141

        assert y == x
        assert x == y
    }

    void testUnrelatedComparablesShouldNeverBeEqual() {
        def x = new Date()
        def n = 3
        assert n != x
        assert x != n
    }

    // until Parrot is merged
    void testNotIdentical() {
        def msg = shouldFail(MultipleCompilationErrorsException) {
            shell.evaluate """
                def x = []
                def y = []
                def doIt() {
                    assert y !== x
                }
                doIt()
            """
        }
        assert msg.contains("!==") && msg.contains("not supported")
    }

    // until Parrot is merged
    void testIdenticalCompileStatic() {
        def msg = shouldFail(MultipleCompilationErrorsException) {
            shell.evaluate """
                def x = []
                def y = []
                @groovy.transform.CompileStatic
                def doIt() {
                    assert y === x
                }
                doIt()
            """
        }
        assert msg.contains("===") && msg.contains("not supported")
    }
}
