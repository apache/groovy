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
package org.apache.groovy.util;

import org.junit.Assert;
import org.junit.Test;

public class ArraysTest {
    @Test
    public void testConcat0() {
        Assert.assertNull(Arrays.concat());
        Assert.assertNull(Arrays.concat(null));
    }

    @Test
    public void testConcat1() {
        Integer[] a = new Integer[] {1, 2};
        Integer[] result = Arrays.concat(a);
        Assert.assertNotSame(a, result);
        Assert.assertArrayEquals(new Integer[] {1, 2}, result);
    }

    @Test
    public void testConcat2() {
        Integer[] a = new Integer[] {1, 2};
        Integer[] b = null;
        Integer[] result = Arrays.concat(a, b);
        Assert.assertNotSame(a, result);
        Assert.assertArrayEquals(new Integer[] {1, 2}, result);
    }

    @Test
    public void testConcat3() {
        Integer[] a = new Integer[] {1, 2};
        Integer[] b = new Integer[0];
        Integer[] result = Arrays.concat(a, b);
        Assert.assertNotSame(a, result);
        Assert.assertArrayEquals(new Integer[] {1, 2}, result);
    }

    @Test
    public void testConcat4() {
        Integer[] a = new Integer[] {1, 2};
        Integer[] b = new Integer[] {3, 4};
        Integer[] result = Arrays.concat(a, b);
        Assert.assertArrayEquals(new Integer[] {1, 2, 3, 4}, result);
    }

    @Test
    public void testConcat5() {
        Integer[] a = new Integer[] {1, 2};
        Integer[] b = new Integer[] {3, 4};
        Integer[] c = new Integer[] {5, 6, 7, 8, 9};
        Integer[] result = Arrays.concat(a, b, c);
        Assert.assertArrayEquals(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9}, result);
    }
}
