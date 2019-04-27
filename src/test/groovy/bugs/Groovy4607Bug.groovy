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

class Groovy4607Bug extends CompilableTestSupport {
    void testProtectedOverridingPublic() {
        def errorMsg = shouldNotCompile('''
            abstract class Super {
                public abstract myMethod()
            }
            class Child extends Super {
                protected myMethod() { true }
            }
            assert new Child() != null
        ''')
        assert errorMsg.contains('cannot override myMethod in Super')
        assert errorMsg.contains('attempting to assign weaker access privileges')
        assert errorMsg.contains('was public')
    }

    void testPrivateOverridingProtected() {
        def errorMsg = shouldNotCompile('''
            class Super {
                protected myMethod(int i, int j) { i + j }
            }
            class Child extends Super {
                private myMethod(int i, int j) { i - j }
            }
            assert new Child() != null
        ''')
      assert errorMsg.contains('cannot override myMethod in Super')
      assert errorMsg.contains('attempting to assign weaker access privileges')
      assert errorMsg.contains('was protected')
    }
}