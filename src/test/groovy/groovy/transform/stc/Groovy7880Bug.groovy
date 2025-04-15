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

package groovy.transform.stc

import gls.CompilableTestSupport

class Groovy7880Bug extends CompilableTestSupport {
    void testDiamondUseShouldNotCauseNPE() {
        shouldCompile '''
            @groovy.transform.CompileStatic
            class BugTest {
                private class CompilerKiller<T> {
                    private T t
                    CompilerKiller(T t){ this.t = t }
                    CompilerKiller(){ }
                }

                void "This works"(){
                    CompilerKiller<BugTest> sample = new CompilerKiller<BugTest>()
                }

                void "This previously caused a NPE"(){
                    CompilerKiller<BugTest> sample = new CompilerKiller<>(this)
                }

                void "This previously caused a NPE as well"(){
                    CompilerKiller<BugTest> sample = new CompilerKiller<>(new BugTest())
                }

                void "This does work"(){
                    CompilerKiller<BugTest> sample = new CompilerKiller<BugTest>(this)
                }

                void "This works as well"(){
                    CompilerKiller<BugTest> sample = new CompilerKiller(this)
                }
            }
        '''
    }
}
