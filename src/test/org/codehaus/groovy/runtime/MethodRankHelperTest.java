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
package org.codehaus.groovy.runtime;

import junit.framework.TestCase;

public class MethodRankHelperTest extends TestCase {

    public void testDELDistance() throws Exception {
        assertEquals("Identical Strings", 0, MethodRankHelper.delDistance("asdf", "asdf"));

        //Simple tests
        assertEquals("Case Start", MethodRankHelper.DL_CASE, MethodRankHelper.delDistance("Asdf", "asdf"));
        assertEquals("Case Mid", MethodRankHelper.DL_CASE, MethodRankHelper.delDistance("aSdf", "asdf"));
        assertEquals("Case End", MethodRankHelper.DL_CASE, MethodRankHelper.delDistance("asdF", "asdf"));

        assertEquals("Del Start", MethodRankHelper.DL_DELETE, MethodRankHelper.delDistance("sdf", "asdf"));
        assertEquals("Del Mid", MethodRankHelper.DL_DELETE, MethodRankHelper.delDistance("adf", "asdf"));
        assertEquals("Del End", MethodRankHelper.DL_DELETE, MethodRankHelper.delDistance("asd", "asdf"));

        assertEquals("Ins Start", MethodRankHelper.DL_DELETE, MethodRankHelper.delDistance("aasdf", "asdf"));
        assertEquals("Ins Mid", MethodRankHelper.DL_DELETE, MethodRankHelper.delDistance("assdf", "asdf"));
        assertEquals("Ins End", MethodRankHelper.DL_DELETE, MethodRankHelper.delDistance("asdff", "asdf"));

        assertEquals("Sub Start", MethodRankHelper.DL_SUBSTITUTION, MethodRankHelper.delDistance("Qsdf", "asdf"));
        assertEquals("Sub Mid", MethodRankHelper.DL_SUBSTITUTION, MethodRankHelper.delDistance("aQdf", "asdf"));
        assertEquals("Sub End", MethodRankHelper.DL_SUBSTITUTION, MethodRankHelper.delDistance("asdQ", "asdf"));

        assertEquals("Tra Start", MethodRankHelper.DL_TRANSPOSITION, MethodRankHelper.delDistance("sadf", "asdf"));
        assertEquals("Tra Mid", MethodRankHelper.DL_TRANSPOSITION, MethodRankHelper.delDistance("adsf", "asdf"));
        assertEquals("Tra End", MethodRankHelper.DL_TRANSPOSITION, MethodRankHelper.delDistance("asfd", "asdf"));

        //A transposition and a casemodification
        assertEquals("TraCase Start1", MethodRankHelper.DL_CASE + MethodRankHelper.DL_TRANSPOSITION,
                MethodRankHelper.delDistance("sAdf", "asdf"));
        assertEquals("TraCase Start2", MethodRankHelper.DL_CASE + MethodRankHelper.DL_TRANSPOSITION,
                MethodRankHelper.delDistance("Sadf", "asdf"));
        assertEquals("TraCase Mid1", MethodRankHelper.DL_CASE + MethodRankHelper.DL_TRANSPOSITION,
                MethodRankHelper.delDistance("aDsf", "asdf"));
        assertEquals("TraCase Mid2", MethodRankHelper.DL_CASE + MethodRankHelper.DL_TRANSPOSITION,
                MethodRankHelper.delDistance("adSf", "asdf"));
        assertEquals("TraCase End1", MethodRankHelper.DL_CASE + MethodRankHelper.DL_TRANSPOSITION,
                MethodRankHelper.delDistance("asFd", "asdf"));
        assertEquals("TraCase End2", MethodRankHelper.DL_CASE + MethodRankHelper.DL_TRANSPOSITION,
                MethodRankHelper.delDistance("asfD", "asdf"));

        assertEquals("2x TraCase1", (MethodRankHelper.DL_CASE + MethodRankHelper.DL_TRANSPOSITION) * 2,
                MethodRankHelper.delDistance("sAfD", "asdf"));
        assertEquals("2x TraCase2", (MethodRankHelper.DL_CASE + MethodRankHelper.DL_TRANSPOSITION) * 2,
                MethodRankHelper.delDistance("SaFd", "asdf"));

        assertEquals("2x TraCase + Case", (MethodRankHelper.DL_CASE + MethodRankHelper.DL_TRANSPOSITION) * 2 + MethodRankHelper.DL_CASE,
                MethodRankHelper.delDistance("SAfD", "asdf"));
        assertEquals("2x TraCase + Case", (MethodRankHelper.DL_CASE + MethodRankHelper.DL_TRANSPOSITION) * 2 + MethodRankHelper.DL_CASE,
                MethodRankHelper.delDistance("SAFd", "asdf"));
        assertEquals("2x TraCase + Case", (MethodRankHelper.DL_CASE + MethodRankHelper.DL_TRANSPOSITION) * 2 + MethodRankHelper.DL_CASE,
                MethodRankHelper.delDistance("sAFD", "asdf"));
        assertEquals("2x TraCase + Case", (MethodRankHelper.DL_CASE + MethodRankHelper.DL_TRANSPOSITION) * 2 + MethodRankHelper.DL_CASE,
                MethodRankHelper.delDistance("SaFD", "asdf"));
    }

    /**
     * turns a int array to a Integer array
     */
    private Integer[] box(int[] ia) {
        Integer[] ret = new Integer[ia.length];
        for (int i = 0; i < ia.length; i++) {
            ret[i] = new Integer(ia[i]);
        }
        return ret;
    }

    public void testDamerauLevenshteinDistance() {
        //There HAS to be a better way to do this!
        assertEquals("Equals", 0, MethodRankHelper.damerauLevenshteinDistance(box(new int[]{0, 1, 2, 3}), box(new int[]{0, 1, 2, 3})));

        assertEquals("Del Start", MethodRankHelper.DL_DELETE, MethodRankHelper.damerauLevenshteinDistance(box(new int[]{0, 1, 2, 3}), box(new int[]{1, 2, 3})));
        assertEquals("Del Mid", MethodRankHelper.DL_DELETE, MethodRankHelper.damerauLevenshteinDistance(box(new int[]{0, 1, 2, 3}), box(new int[]{0, 2, 3})));
        assertEquals("Del End", MethodRankHelper.DL_DELETE, MethodRankHelper.damerauLevenshteinDistance(box(new int[]{0, 1, 2, 3}), box(new int[]{0, 1, 2})));

        assertEquals("Sub Start", MethodRankHelper.DL_SUBSTITUTION, MethodRankHelper.damerauLevenshteinDistance(box(new int[]{0, 1, 2, 3}), box(new int[]{4, 1, 2, 3})));
        assertEquals("Sub Mid", MethodRankHelper.DL_SUBSTITUTION, MethodRankHelper.damerauLevenshteinDistance(box(new int[]{0, 1, 2, 3}), box(new int[]{0, 4, 2, 3})));
        assertEquals("Sub End", MethodRankHelper.DL_SUBSTITUTION, MethodRankHelper.damerauLevenshteinDistance(box(new int[]{0, 1, 2, 3}), box(new int[]{0, 1, 2, 4})));

        assertEquals("Ins Start", MethodRankHelper.DL_DELETE, MethodRankHelper.damerauLevenshteinDistance(box(new int[]{0, 1, 2, 3}), box(new int[]{4, 0, 1, 2, 3})));
        assertEquals("Ins Mid", MethodRankHelper.DL_DELETE, MethodRankHelper.damerauLevenshteinDistance(box(new int[]{0, 1, 2, 3}), box(new int[]{0, 1, 4, 2, 3})));
        assertEquals("Ins End", MethodRankHelper.DL_DELETE, MethodRankHelper.damerauLevenshteinDistance(box(new int[]{0, 1, 2, 3}), box(new int[]{0, 1, 2, 3, 4})));
    }

    public void boxVarTest() throws Exception {
        assertEquals(Boolean.class, MethodRankHelper.boxVar(Boolean.TYPE));
        assertEquals(Character.class, MethodRankHelper.boxVar(Character.TYPE));
        assertEquals(Byte.class, MethodRankHelper.boxVar(Byte.TYPE));
        assertEquals(Double.class, MethodRankHelper.boxVar(Double.TYPE));
        assertEquals(Float.class, MethodRankHelper.boxVar(Float.TYPE));
        assertEquals(Integer.class, MethodRankHelper.boxVar(Integer.TYPE));
        assertEquals(Long.class, MethodRankHelper.boxVar(Long.TYPE));
        assertEquals(Short.class, MethodRankHelper.boxVar(Short.TYPE));

        assertEquals(MethodRankHelperTest.class, MethodRankHelper.boxVar(MethodRankHelperTest.class));
    }

    /*
    A better set of more complete tests of everything below is on the way!
    */
    final class TempClass {
        int x, y, fieldWithLongName;

        public TempClass(int x, int y) {
        }

        public TempClass(int x) {
        }

        public TempClass() {
        }

        public int getX() {
            return x;
        }

        public int gety() {
            return y;
        }

        public void setX(int x) {
        }

        public void setXY(int x, int y) {
        }

        public int getXYMultiplied() {
            return x * y;
        }
    }

    public void testGetMethodSuggestionString() {
        assertEquals("No suggestion", 0, MethodRankHelper.getMethodSuggestionString("noSuchMethod", TempClass.class, new Object[]{}).length());
        assertEquals("getx", true, MethodRankHelper.getMethodSuggestionString("getx", TempClass.class, new Object[]{}).indexOf("getX") > 0);
        assertEquals("To great distance", true, MethodRankHelper.getMethodSuggestionString("getx", TempClass.class, new Object[]{}).indexOf("getXYMultiplied") < 0);
    }

    public void testGetConstructorSuggestionString() {
        assertEquals("No suggestion", 0, MethodRankHelper.getConstructorSuggestionString(TempClass.class, new Object[]{null, null, null, null, null}).length());
    }
}