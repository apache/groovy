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

import groovy.test.GroovyTestCase

class PrimitiveArraysTest extends GroovyTestCase {

    def c1Field = [] as char[]
    char[] c2Field = [] as char[]
    
    def i1Field = [] as int[]
    int[] i2Field = [] as int[]

    def d1Field = [] as double[]
    double[] d2Field = [] as double[]

    def f1Field = [] as float[]
    float[] f2Field = [] as float[]
    
    def l1Field = [] as long[]
    long[] l2Field = [] as long[]    

    def b1Field = [] as byte[]
    byte[] b2Field = [] as byte[]
    
    def s1Field = [] as short[]
    short[] s2Field = [] as short[]
    
    void testChar() {
        assert c1Field.class == c2Field.class
        def ca = ['l','l'] as char[]
        char[] cb = ['l','l']
        assert ca.class == cb.class
        assert c1Field.class == ca.class
        assert ca.class.name == "[C"
        
        ca.each{ assert it=='l' }
        cb.each{ assert it=='l' }
    }


    void testInt() {
        assert i1Field.class == i2Field.class
        def ia = [1,1] as int[]
        int[] ib = [1,1]
        assert ia.class == ib.class
        assert i1Field.class == ia.class
        assert ia.class.name == "[I"
        
        ia.each{ assert it==1 }
        ib.each{ assert it==1 }
    }


    void testLong() {
        assert l1Field.class == l2Field.class
        def la = [1,1] as long[]
        long[] lb = [1,1]
        assert la.class == lb.class
        assert l1Field.class == la.class
        assert la.class.name == "[J"
        
        la.each{ assert it==1l }
        lb.each{ assert it==1l }
    }


    void testShort() {
        assert s1Field.class == s2Field.class
        def sa = [1,1] as short[]
        short[] sb = [1,1]
        assert sa.class == sb.class
        assert s1Field.class == sa.class
        assert sa.class.name == "[S"
        
        sa.each{ assert it==1 }
        sb.each{ assert it==1 }
    }

    void testByte() {
        assert b1Field.class == b2Field.class
        def ba = [1,1] as byte[]
        byte[] bb = [1,1]
        assert ba.class == bb.class
        assert b1Field.class == ba.class
        assert ba.class.name == "[B"
        
        ba.each{ assert it==1 }
        bb.each{ assert it==1 }
    }
    
    
    void testDouble() {
        assert d1Field.class == d2Field.class
        def da = [1,1] as double[]
        double[] db = [1,1]
        assert da.class == db.class
        assert d1Field.class == da.class
        assert da.class.name == "[D"
        
        da.each{ assert it==1.0d }
        db.each{ assert it==1.0d }
    }

    void testFloat() {
        assert f1Field.class == f2Field.class
        def fa = [1,1] as float[]
        float[] fb = [1,1]
        assert fa.class == fb.class
        assert f1Field.class == fa.class
        assert fa.class.name == "[F"
        
        fa.each{ assert it==1.0f }
        fb.each{ assert it==1.0f }
    }
    
    void testBoolean() {
      def ba = new boolean[1][2][3]
      assert ba[0].length == 2
      assert ba[0][0].length == 3
      ba = [true,true] as boolean[]
      ba.each { assert it==true }
      assert ba.class.name == "[Z"
    }

}