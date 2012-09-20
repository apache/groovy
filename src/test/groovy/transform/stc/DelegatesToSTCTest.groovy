/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.transform.stc

/**
 * Units tests aimed at testing the behaviour of {@link DelegatesTo} in combination
 * with static type checking.
 *
 * @author Cedric Champeau
 */
class DelegatesToSTCTest extends StaticTypeCheckingTestCase {
    void testShouldAcceptMethodCall() {
        assertScript '''
            class ExecSpec {
                boolean called = false
                void foo() {
                    called = true
                }
            }

            ExecSpec spec = new ExecSpec()

            void exec(ExecSpec spec, @DelegatesTo(value=ExecSpec, strategy=Closure.DELEGATE_FIRST) Closure param) {
                param.delegate = spec
                param()
            }

            exec(spec) {
                foo() // should be recognized because param is annotated with @DelegatesTo(ExecSpec)
            }
            assert spec.isCalled()
        '''
    }

}
