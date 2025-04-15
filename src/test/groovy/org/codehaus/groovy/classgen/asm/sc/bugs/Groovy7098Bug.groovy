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






package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport

class Groovy7098Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {
    void testForEachLoopWithIterable() {
        assertScript '''
Iterable<Object> toIterable(List list) {
    return new Iterable<Object>() {
        Iterator iterator() {
            list.iterator()
        }
    }
}

List loopDirect(List list) {
    List result = new ArrayList()
    for (Object o : list) {
        result.add(o)
        result.add(o)
    }
    result
}

List loopIterable(List list) {
    List result = new ArrayList()
    for (Object o : toIterable(list)) {
        result.add(o)
        result.add(o)
    }
    result
}

def list = ['a','b']
assert loopDirect(list) == ['a','a','b','b']
assert loopIterable(list) == ['a','a','b','b']
        '''
    }
}
