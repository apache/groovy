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

import gls.CompilableTestSupport

class Groovy7909Bug extends CompilableTestSupport {
    void testDynamicCompile(){
        shouldCompile '''
trait Three implements One, Two {
    def postMake() {
        One.super.postMake()
        Two.super.postMake()
        println "Three"
    }
}
trait One {
    def postMake() { println "One"}
}
trait Two {
    def postMake() { println "Two"}
}
class Four implements Three {
    def make() {
        Three.super.postMake()
        println "All done?"
    }
}
Four f = new Four()
f.make()
    '''
    }
    void testStaticCompile(){
        shouldCompile '''
@groovy.transform.CompileStatic
trait Three implements One, Two {
    def postMake() {
        One.super.postMake()
        Two.super.postMake()
        println "Three"
    }
}
trait One {
    def postMake() { println "One"}
}
trait Two {
    def postMake() { println "Two"}
}
class Four implements Three {
    def make() {
        Three.super.postMake()
        println "All done?"
    }
}
Four f = new Four()
f.make()
    '''
    }
}

