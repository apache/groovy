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
package bugs

import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy12140 {

    private static final String SCRIPT = '''
        class W {
            int num(int... xs) { 42 }
            int val() { 42 }
            char ch() { (char) 'a' }
            boolean flag() { true }
            long lng() { 42L }
            byte byt() { (byte) 42 }
            short shrt() { (short) 42 }
        }
        def w = new W()
        def name = 'val'
        // dynamic-name calls dispatch reflectively via doMethodInvoke, in both
        // compilation modes: primitive returns must box through the valueOf
        // caches for identity (===) parity with directly dispatched calls
        // (float/double are deliberately not asserted: valueOf caches nothing
        // for them on any dispatch path)
        assert w."$name"() === 42
        assert w."${'ch'}"() === Character.valueOf((char) 'a')
        assert w."${'flag'}"() === Boolean.TRUE
        assert w."${'lng'}"() === Long.valueOf(42L)
        assert w."${'byt'}"() === Byte.valueOf((byte) 42)
        assert w."${'shrt'}"() === Short.valueOf((short) 42)
        // varargs methods use the reflective call-site variant under classic
        assert w.num(1, 2) === 42
    '''

    @Test
    void testPrimitiveReturnBoxIdentity() {
        assertScript SCRIPT
    }

    @Test
    void testPrimitiveReturnBoxIdentityClassic() {
        def config = new CompilerConfiguration()
        config.optimizationOptions.indy = false
        new GroovyShell(config).evaluate SCRIPT
    }
}
