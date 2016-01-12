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

import org.codehaus.groovy.runtime.InvokerHelper

class Groovy4414Bug extends GroovyTestCase {
    public void testUnaryMinus() {
        assertEquals(-1,    InvokerHelper.unaryMinus(1)); // relying here on boxing for Integer
        assertEquals(-1l,   InvokerHelper.unaryMinus(1l));
        assertEquals(-1.0f, InvokerHelper.unaryMinus(1.0f));
        assertEquals(-1.0,  InvokerHelper.unaryMinus(1.0));
        assertEquals(BigInteger.valueOf(-1l),  InvokerHelper.unaryMinus(BigInteger.valueOf(1l)));
        assertEquals(BigDecimal.valueOf(-1l),  InvokerHelper.unaryMinus(BigDecimal.valueOf(1l)));
        
        List<Object> expected = new ArrayList<Object>() {{
            add(-1);
            add(-1.0);
            add(-1l);
        }};
        
        List<Object> actual = new ArrayList<Object>() {{
            add(1);
            add(1.0);
            add(1l);
        }};
        
        assertEquals(expected, InvokerHelper.unaryMinus(actual));
        
        assertEquals((short)-1, InvokerHelper.unaryMinus((short)1));
        assertEquals((byte)-1, InvokerHelper.unaryMinus((byte)1));
    }

    public void testUnaryPlus() {
        assertEquals(1,    InvokerHelper.unaryPlus(1)); // relying here on boxing for Integer
        assertEquals(1l,   InvokerHelper.unaryPlus(1l));
        assertEquals(1.0f, InvokerHelper.unaryPlus(1.0f));
        assertEquals(1.0,  InvokerHelper.unaryPlus(1.0));
        assertEquals(BigInteger.valueOf(1l),  InvokerHelper.unaryPlus(BigInteger.valueOf(1l)));
        assertEquals(BigDecimal.valueOf(1l),  InvokerHelper.unaryPlus(BigDecimal.valueOf(1l)));
        
        List<Object> actual = new ArrayList<Object>() {{
            add(1);
            add(1.0);
            add(1l);
        }};
        
        assertEquals(actual, InvokerHelper.unaryPlus(actual));
        
        assertEquals((short)1, InvokerHelper.unaryPlus((short)1));
        assertEquals((byte)1, InvokerHelper.unaryPlus((byte)1));
    }
}
