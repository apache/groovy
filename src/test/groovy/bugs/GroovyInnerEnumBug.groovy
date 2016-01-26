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

class GroovyInnerEnumBug extends GroovyTestCase {
    static public enum MyEnum { 
        a, b, c
        public static MyEnum[] myenums = [a, b, c];
    }
    
    // GROOVY-3979
    void testEnumInsideAClass3979() {
        assertScript """
            class EnumTest2 {
                enum Direction3979 { North, East, South, West }
                static void main(args) {
                    for (d in Direction3979) { 
                        assert d instanceof Direction3979
                    }
                }
            }
        """
    }

    // GROOVY-3994
    void testEnumInsideAClass3994() {
        assert MyEnum.a.name() == 'a'
        assertTrue Enum.isAssignableFrom(MyEnum.class)
        assert EnumSet.allOf(MyEnum.class) instanceof EnumSet 
    }
}
