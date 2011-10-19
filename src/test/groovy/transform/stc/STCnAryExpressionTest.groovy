/*
 * Copyright 2003-2010 the original author or authors.
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
package groovy.transform.stc

/**
 * Unit tests for static type checking : unary and binary expressions.
 *
 * @author Cedric Champeau
 */
class STCnAryExpressionTest extends StaticTypeCheckingTestCase {

    void testBinaryStringPlus() {
        assertScript """
            String str = 'a'
            String str2 = 'b'
            str+str2
        """
    }

    void testBinaryStringPlusInt() {
        assertScript """
            String str = 'a'
            int str2 = 2
            str+str2
        """
    }

    void testBinaryObjectPlusInt() {
        shouldFailWithMessages """
            def str = new Object()
            int str2 = 2
            str+str2
        """, "Cannot find matching method plus(java.lang.Object, java.lang.Integer)",
                "tbd" // todo : remove when transformation will be fixed
    }

    void testBinaryIntPlusObject() {
        shouldFailWithMessages """
            def str = new Object()
            int str2 = 2
            str2+str
        """, "Cannot find matching method plus(java.lang.Integer, java.lang.Object)",
                "tbd" // todo : remove when transformation will be fixed
    }

    void testPrimitiveComparison() {
        assertScript '''
            1<2
        '''

        assertScript '''
            1>2
        '''

        assertScript '''
            1<=2
        '''

        assertScript '''
            1>=2
        '''

        assertScript '''
            1==2
        '''
    }

    void testBoxedTypeComparison() {
        assertScript '''
            1<new Integer(2)
        '''

        assertScript '''
            1>new Integer(2)
        '''

        assertScript '''
            1<=new Integer(2)
        '''

        assertScript '''
            1>=new Integer(2)
        '''

        assertScript '''
            1==new Integer(2)
        '''
    }

}

