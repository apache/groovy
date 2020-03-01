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
package groovy.bugs.groovy9081

import groovy.bugs.groovy9081.somepkg.ProtectedConstructor
import org.junit.Test

import java.awt.Font
import java.awt.HeadlessException
import java.lang.annotation.RetentionPolicy

// TODO add JVM option `--illegal-access=deny` when all warnings fixed
final class Groovy9081 {

    @Test
    void testAccessPublicMemberOfPrivateClass() {
        def m = Collections.unmodifiableMap([:])
        assert null != m.toString()
        assert null == m.get(0)
    }

    @Test
    void testFavorMethodWithExactParameterType() {
        def em1 = new EnumMap(RetentionPolicy.class)
        def em2 = new EnumMap(RetentionPolicy.class)

        assert em2 == em1
    }

    @Test
    void testShouldChoosePublicGetterInsteadOfPrivateField1() {
        def f = Integer.class.getDeclaredField("MIN_VALUE")
        assert 0 != f.modifiers
    }

    @Test
    void testShouldChoosePublicGetterInsteadOfPrivateField2() {
        def f = new Font("Monospaced", Font.PLAIN, 12)
        assert f.name
    }

    @Test
    void testGetPropertiesOfObjects() {
        assert null != ''.properties
    }

    @Test
    void testAsType1() {
        [run: {}] as TimerTask
    }

    @Test
    void testAsType2() {
        [run: {}] as ProtectedConstructor
    }

    @Test
    void testAccessPackagePrivateInnerClassMember() {
        def m = new HashMap()
        m.a = 69
        m.entrySet().iterator().next().toString()
    }

    @Test
    void testAccessPackagePrivateMethod() {
        BigInteger a = 333
        int b = 2
        BigDecimal c = a * b
        assert c == 666
    }

    @Test
    void testGetProperty() {
        try {
            java.awt.Toolkit.defaultToolkit.systemClipboard
        } catch (HeadlessException ignore) {
        }
    }
}
