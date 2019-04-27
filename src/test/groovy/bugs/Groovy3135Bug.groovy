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

class Groovy3135Bug extends GroovyTestCase {
    static Byte b = Byte.parseByte("1")
    static Short s = Short.parseShort("2")
    static Integer i = Integer.parseInt("3")
    static Long l = Long.parseLong("4")
    static Float f = Float.parseFloat("5")
    static Double d = Double.parseDouble("6")
    static BigInteger bi = new BigInteger("7")
    static BigDecimal bd = new BigDecimal("8")

    def values

    void testConversionForPrimitiveTypeVarArgs() {
        
        setVarArgsShort("", b, s)
        checkConversionAndVarArgCount(Short.TYPE, 2)

        setVarArgsInteger("", b, s, i)
        checkConversionAndVarArgCount(Integer.TYPE, 3)

        setVarArgsLong("", b, s, i, l)
        checkConversionAndVarArgCount(Long.TYPE, 4)
        
        setVarArgsFloat("", b, s, i, l, f)
        checkConversionAndVarArgCount(Float.TYPE, 5)
        
        setVarArgsDouble("", b, s, i, l, f, d, bi, bd)
        checkConversionAndVarArgCount(Double.TYPE, 8)
    }

    def setVarArgsShort(String str, short... varArgValues) {
        values = varArgValues
    }
    
    def setVarArgsInteger(String str, int... varArgValues) {
        values = varArgValues
    }
    
    def setVarArgsLong(String str, long... varArgValues) {
        values = varArgValues
    }
    
    def setVarArgsFloat(String str, float... varArgValues) {
        values = varArgValues
    }
    
    def setVarArgsDouble(String str, double... varArgValues) {
        values = varArgValues
    }

    def checkConversionAndVarArgCount(expectedType, varArgsCount) {
        assert values.class.componentType == expectedType
        assert values.size() == varArgsCount
    }
}
