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

import groovy.test.GroovyTestCase

/**
 * Fix Bug GROOVY-683
 */
class PrimitivePropertyBug extends GroovyTestCase {
     
    double x1
    float x2
    long x3
    int x4
    short x5
    byte x6
    char x7

    void testBug() {
        def y = new PrimitivePropertyBug()
        y.x1 = 10.0
        y.x2 = 10.0
        y.x3 = 10.0
        y.x4 = 10.0
        y.x5 = 10.0
        y.x6 = 10.0
        y.x7 = 10.0
        
        assert y.x1 == 10.0
        assert y.x2 == 10.0
        assert y.x3 == 10.0
        assert y.x4 == 10.0
        assert y.x5 == 10.0
        assert y.x6 == 10.0
        assert y.x1.class == Double.class
        assert y.x2.class == Float.class
        assert y.x3.class == Long.class
        assert y.x4.class == Integer.class
        assert y.x5.class == Short.class
        assert y.x6.class == Byte.class
        assert y.x7.class == Character.class
        assert y.x1 + y.x1 == y.x1 * 2
        assert y.x2 - 1 == 9.0f
        assert y.x3 * 2 == 20L
        assert y.x4 == 10
        assert y.x5 == 10
        assert y.x6 + 3 == 13
        assert "Hello" + y.x7 + "World!" == "Hello\nWorld!"
    }
}
