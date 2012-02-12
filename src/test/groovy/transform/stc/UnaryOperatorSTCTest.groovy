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
 * Unit tests for static type checking : unary operator tests.
 *
 * @author Cedric Champeau
 */
class UnaryOperatorSTCTest extends StaticTypeCheckingTestCase {

     void testUnaryPlusOnInt() {
         assertScript '''
            int x = 1
            assert +x == 1
         '''
     }

     void testUnaryPlusOnInteger() {
         assertScript '''
            Integer x = new Integer(1)
            assert +x == 1
         '''
     }

     void testUnaryMinusOnInt() {
         assertScript '''
            int x = 1
            assert -x == -1
         '''
     }

     void testUnaryMinusOnInteger() {
         assertScript '''
            Integer x = new Integer(1)
            assert -x == -1
         '''
     }

     void testUnaryPlusOnShort() {
         assertScript '''
            short x = 1
            assert +x == 1
         '''
     }

     void testUnaryPlusOnBoxedShort() {
         assertScript '''
            Short x = new Short((short)1)
            assert +x == 1
         '''
     }

     void testUnaryMinusOnShort() {
         assertScript '''
            short x = 1
            assert -x == -1
         '''
     }

     void testUnaryMinusOnBoxedShort() {
         assertScript '''
            Short x = new Short((short)1)
            assert -x == -1
         '''
     }

     void testUnaryPlusOnFloat() {
         assertScript '''
            float x = 1f
            assert +x == 1f
         '''
     }

     void testUnaryPlusOnBoxedFloat() {
         assertScript '''
            Float x = new Float(1f)
            assert +x == 1f
         '''
     }

     void testUnaryMinusOnFloat() {
         assertScript '''
            float x = 1f
            assert -x == -1f
         '''
     }

     void testUnaryMinusOnBoxedFloat() {
         assertScript '''
            Float x = new Float(1f)
            assert -x == -1f
         '''
     }

     void testUnaryPlusOnDouble() {
         assertScript '''
            double x = 1d
            assert +x == 1d
         '''
     }

     void testUnaryPlusOnBoxedDouble() {
         assertScript '''
            Double x = new Double(1d)
            assert +x == 1d
         '''
     }

     void testUnaryMinusOnDouble() {
         assertScript '''
            double x = 1d
            assert -x == -1d
         '''
     }

     void testUnaryMinusOnBoxedDouble() {
         assertScript '''
            Double x = new Double(1d)
            assert -x == -1d
         '''
     }


}

