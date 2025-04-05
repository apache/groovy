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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

final class InstanceofTest {

    @Test
    void testIsInstance() {
        def o = 12

        assert (o instanceof Integer)
    }

    @Test
    void testNotInstance() {
        def o = 12

        assert !(o instanceof Double)
    }

    @Test
    void testImportedClass() {
        def m = ["xyz":2]

        assert  (m  instanceof Map)
        assert !(m !instanceof Map)
        assert !(m  instanceof Double)
        assert  (m !instanceof Double)
    }

    @Test
    void testFullyQualifiedClass() {
        def l = [1, 2, 3]

        assert (l instanceof java.util.List)
        assert !(l instanceof java.util.Map)
        assert (l !instanceof java.util.Map)
    }

    @Test
    void testBoolean() {
       assert true instanceof Object
       assert true==true instanceof Object
       assert true==false instanceof Object
       assert true==false instanceof Boolean
       assert !new Object() instanceof Boolean
    }

    // GROOVY-11585
    @Test
    void testGenerics() {
        assert [] instanceof List<?>

        def err = shouldFail '''
            def x = ([] instanceof List<String>)
        '''
        assert err =~ 'Cannot perform instanceof check against parameterized type List<String>'
    }

    // GROOVY-11229
    @Test
    void testVariable() {
        def n = (Number) 12345
        if (n instanceof Integer i) {
            assert i.intValue() == 12345
        } else {
            assert false : 'expected Integer'
        }
        if (n instanceof String s) {
            assert false : 'not String'
        } else {
            assert n.intValue() == 12345
        }
        assert (n instanceof Integer i && i.intValue() == 12345)
    }

    // GROOVY-11229
    @Test
    void testVariable2() {
        assert transformString(null) == null
        assert transformString(1234) == 1234
        assert transformString('xx') == 'XX'
    }

    def transformString(o) {
        o instanceof String s ? s.toUpperCase() : o
    }

    // GROOVY-11229
    @Test
    void testVariableScope() {
        def err = shouldFail '''
            def x = null
            if (x instanceof String s) {
            } else {
                s
            }
        '''
        assert err =~ /No such property: s/

        def shell = GroovyShell.withConfig {
            ast groovy.transform.TypeChecked
        }

        err = shouldFail shell, '''
            Number n = 12345
            if (n instanceof Integer i) {
            }
            i.toString()
        '''
        assert err =~ /The variable .i. is undeclared\.\s+@ line 5, column 13/

        err = shouldFail shell, '''
            Number n = 12345
            if (n instanceof Integer i) ; else {
                i.toString()
            }
        '''
        assert err =~ /The variable .i. is undeclared\.\s+@ line 4, column 17/

        err = shouldFail shell, '''
            Number n = 12345
            while (n instanceof Integer i) {
                n = i.doubleValue()
            }
            i.toString()
        '''
        assert err =~ /The variable .i. is undeclared\.\s+@ line 6, column 13/

        err = shouldFail shell, '''
            Number n = 12345
            do {
                n = n.doubleValue()
            } while (n instanceof Integer i)
            i.toString()
        '''
        assert err =~ /The variable .i. is undeclared\.\s+@ line 6, column 13/

        err = shouldFail shell, '''
            Number n = 12345
            do {
                i.toString()
            } while (n instanceof Integer i && (n = i.doubleValue()))
        '''
        assert err =~ /The variable .i. is undeclared\.\s+@ line 4, column 17/

        err = shouldFail shell, '''
            Number n = 12345
            switch (n instanceof Integer i) {
              case true:
                i.toString()
              case false:
                i.toString()
            }
            i.toString()
        '''
        assert err =~ /The variable .i. is undeclared\.\s+@ line 9, column 13/

        err = shouldFail shell, '''
            Number n = 12345
            return (n instanceof Integer i && i.intValue() == 12345)
            i.toString()
        '''
        assert err =~ /The variable .i. is undeclared\.\s+@ line 4, column 13/

        err = shouldFail shell, '''
            Number n = 12345
            assert (n instanceof Integer i && i.intValue() == 12345)
            i.toString()
        '''
        assert err =~ /The variable .i. is undeclared\.\s+@ line 4, column 13/

        err = shouldFail shell, '''
            Number n = 12345
            print(n instanceof Integer i && i.doubleValue())
            i.toString()
        '''
        assert err =~ /The variable .i. is undeclared\.\s+@ line 4, column 13/

        err = shouldFail shell, '''
            Number n = 12345;
            {
                print(n instanceof Integer i && i.doubleValue())
            }
            i.toString()
        '''
        assert err =~ /The variable .i. is undeclared\.\s+@ line 6, column 13/

        err = shouldFail shell, '''
            Number n = 12345
            boolean b = (n instanceof Integer i && i.intValue())
            i.toString()
        '''
        assert err =~ /The variable .i. is undeclared\.\s+@ line 4, column 13/
    }
}
