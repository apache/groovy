/*
 * Copyright 2003-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy

import gls.CompilableTestSupport

class InterfaceTest extends CompilableTestSupport {

    void testGenericsInInterfaceMembers() {
        // control
        shouldCompile """
        interface I1 {
            public <T> T copy1(T arg)
            public <U extends CharSequence> U copy2(U arg)
            public <V, W> V copy3(W arg)
            public <N extends Number> void foo()
        }
        """
        // erroneous
        shouldNotCompile "interface I2 { public <?> copy1(arg) }"
        shouldNotCompile "interface I3 { public <? extends CharSequence> copy1(arg) }"
    }
}
