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
package groovy.lang;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class MetaClassTest {

    @Test
    void testMetaClass() {
        Class<?> foo = String[].class;
        System.out.println(foo + " name: " + foo.getName());

        MetaClass metaClass = InvokerHelper.getMetaClass(this);

        assertNotNull(metaClass, "got metaclass");

        metaClass.invokeMethod(this, "doSomething", new Object[0]);
    }

    @Test
    void testArray() {
        String[] value = new String[]{"hello"};

        MetaClass metaClass = InvokerHelper.getMetaClass(value);

        assertNotNull(metaClass, "got metaclass");

        metaClass.invokeMethod(value, "toString", new Object[0]);
    }

    @Test
    void testString() {
        String value = "hello";

        MetaClass metaClass = InvokerHelper.getMetaClass(value);

        assertNotNull(metaClass, "got metaclass");

        Object answer = metaClass.invokeMethod(value, "toString", new Object[0]);

        assertEquals("hello", answer);
    }

    @Test
    void testObject() {
        Object value = new Object();

        MetaClass metaClass = InvokerHelper.getMetaClass(value);

        assertNotNull(metaClass, "got metaclass");

        metaClass.invokeMethod(value, "toString", new Object[0]);
    }

    @Test
    void testPublicField() {
        DymmyClass dymmyClass = new DymmyClass();

        MetaClass metaClass = InvokerHelper.getMetaClass(dymmyClass);

        assertEquals(metaClass.getProperty(dymmyClass, "x"), Integer.valueOf(0));
        assertEquals(metaClass.getProperty(dymmyClass, "y"), "none");

        metaClass.setProperty(dymmyClass, "x", Integer.valueOf(25));
        assertEquals(dymmyClass.x, 25);

        metaClass.setProperty(dymmyClass, "y", "newvalue");
        assertEquals(dymmyClass.y, "newvalue");
    }

    // GROOVY-11781
    @Test
    void testMethodMissing() {
        MetaClass metaClass = InvokerHelper.getMetaClass(DymmyClass.class);

        assertThrows(MissingMethodException.class, () -> {
            metaClass.invokeMissingMethod(DymmyClass.class, "xxx", new Object[0]);
        });
    }

    @Test
    void testSetPropertyWithInt() {
        DymmyClass dymmyClass = new DymmyClass();
        MetaClass metaClass = InvokerHelper.getMetaClass(dymmyClass);
        metaClass.setProperty(dymmyClass, "anInt", Integer.valueOf(10));
    }

    @Test
    void testSetPropertyWithDoubleArray() {
        DymmyClass dymmyClass = new DymmyClass();
        MetaClass metaClass = InvokerHelper.getMetaClass(dymmyClass);
        Double[][] matrix2 =
                {
                        {
                                Double.valueOf(35), Double.valueOf(50), Double.valueOf(120)
                        },
                        {
                                Double.valueOf(75), Double.valueOf(80), Double.valueOf(150)
                        }
                };
        metaClass.setProperty(dymmyClass, "matrix", matrix2);
        metaClass.setProperty(dymmyClass, "matrix2", matrix2);
    }

    @Test
    void testSetPropertyWithArray() {
        DymmyClass dymmyClass = new DymmyClass();
        MetaClass metaClass = InvokerHelper.getMetaClass(dymmyClass);

        // test int[]
        int[] ints = new int[]{
                0, 1, 2, 3
        };
        metaClass.setProperty(dymmyClass, "ints", ints);
        assertEquals(ints, metaClass.getProperty(dymmyClass, "ints"));

        // test Integer[]
        Integer[] integers = new Integer[]{
                Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)
        };
        metaClass.setProperty(dymmyClass, "integers", integers);
        assertEquals(integers, metaClass.getProperty(dymmyClass, "integers"));
    }

    @Test
    void testSetPropertyWithList() {
        DymmyClass dymmyClass = new DymmyClass();
        MetaClass metaClass = InvokerHelper.getMetaClass(dymmyClass);

        // test list
        var list = new ArrayList<Integer>();
        list.add(Integer.valueOf(120));
        list.add(Integer.valueOf(150));

        // test int[]
        metaClass.setProperty(dymmyClass, "ints", list);

        // test Integer[]
        metaClass.setProperty(dymmyClass, "integers", list);
    }

    @Test
    void testMetaMethodsOnlyAddedOnce() {
        MetaClass metaClass = InvokerHelper.getMetaClass("some String");

        List<MetaMethod> methods = metaClass.getMetaMethods();
        for (Iterator<MetaMethod> iter = methods.iterator(); iter.hasNext(); ) {
            MetaMethod method = iter.next();
            int count = 0;
            for (Iterator<MetaMethod> inner = methods.iterator(); inner.hasNext(); ) {
                MetaMethod runner = inner.next();
                if (method.equals(runner)) {
                    System.out.println("runner = " + runner);
                    System.out.println("method = " + method);
                    count++;
                }
            }
            assertEquals(1, count, "count of Method " + method.getName());
        }
    }

    //--------------------------------------------------------------------------

    public void doSomething() {
        System.out.println("Called doSomething()");
    }

    static class DymmyClass {
        public int x = 0;
        public String y = "none";
        private int anInt;
        private int[] ints;
        private Integer[] integers;
        double[][] matrix2;
        Double[][] matrix;

        public Integer[] getIntegers() {
            return integers;
        }

        public void setIntegers(Integer[] integers) {
            this.integers = integers;
        }

        public int[] getInts() {
            return ints;
        }

        public void setInts(int[] ints) {
            this.ints = ints;
        }

        public int getAnInt() {
            return anInt;
        }

        public void setAnInt(int anInt) {
            this.anInt = anInt;
        }

        public void setMatrix(Double[][] matrix) {
            this.matrix = matrix;
        }

        public void setMatrix2(double[][] matrixReloaded) {
            this.matrix2 = matrixReloaded;
        }

        public Object methodMissing(String name, Object args) {
            throw new MissingMethodException(name, getClass(), InvokerHelper.asArray(args));
        }
    }
}
