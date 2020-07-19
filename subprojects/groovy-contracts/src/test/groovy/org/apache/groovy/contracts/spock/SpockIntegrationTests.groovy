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
package org.apache.groovy.contracts.spock

import groovy.contracts.Requires
import spock.lang.Specification
import org.apache.groovy.contracts.PreconditionViolation

class ContractsSpec extends Specification {
    def "contracted method with precondition violation"(String dir, String file, String path) {
        when:
        contractedMethod(dir, file, path)
        then:
        thrown(PreconditionViolation)
        where:
        dir | file | path
        42  | ''   | null
    }

    @Requires({ dir && file && path })
    private contractedMethod(String dir, String file, String path) { }

    @spock.lang.Requires({ count < max })
    def "spock Requires annotation still works with groovy-contracts"(Integer count, Integer max) {
        expect:
        count < max
        where:
        count | max
        10    | 20
        20    | 10 // should be aborted/ignored and not throw a groovy-contracts related exception
    }
}
