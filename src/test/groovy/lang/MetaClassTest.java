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

import groovy.util.GroovyTestCase;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MetaClassTest extends GroovyTestCase {

    public void testMetaClass() {
        Class foo = String[].class;
        System.out.println(foo + " name: " + foo.getName());

        MetaClass metaClass = InvokerHelper.getMetaClass(this);

        assertTrue("got metaclass", metaClass != null);

        metaClass.invokeMethod(this, "doSomething", new Object[0]);
    }

    public void testArray() {
        String[] value = new String[]{"hello"};

        MetaClass metaClass = InvokerHelper.getMetaClass(value);

        assertTrue("got metaclass", metaClass != null);

        metaClass.invokeMethod(value, "toString", new Object[0]);
    }

    public void testString() {
        String value = "hello";

        MetaClass metaClass = InvokerHelper.getMetaClass(value);

        assertTrue("got metaclass", metaClass != null);

        Object answer = metaClass.invokeMethod(value, "toString", new Object[0]);

        assertEquals("hello", answer);
    }

    public void testObject() {
        Object value = new Object();

        MetaClass metaClass = InvokerHelper.getMetaClass(value);

        assertTrue("got metaclass", metaClass != null);

        metaClass.invokeMethod(value, "toString", new Object[0]);
    }

    public void testPublicField() {
        DymmyClass dymmyClass = new DymmyClass();

        MetaClass metaClass = InvokerHelper.getMetaClass(dymmyClass);

        assertEquals(metaClass.getProperty(dymmyClass, "x"), new Integer(0));
        assertEquals(metaClass.getProperty(dymmyClass, "y"), "none");

        metaClass.setProperty(dymmyClass, "x", new Integer(25));
        assertEquals(dymmyClass.x, 25);

        metaClass.setProperty(dymmyClass, "y", "newvalue");
        assertEquals(dymmyClass.y, "newvalue");
    }

    public void testSetPropertyWithInt() {
        DymmyClass dymmyClass = new DymmyClass();
        MetaClass metaClass = InvokerHelper.getMetaClass(dymmyClass);
        metaClass.setProperty(dymmyClass, "anInt", new Integer(10));
    }

    public void testSetPropertyWithDoubleArray() {
        DymmyClass dymmyClass = new DymmyClass();
        MetaClass metaClass = InvokerHelper.getMetaClass(dymmyClass);
        Double[][] matrix2 =
                {
                        {
                                new Double(35), new Double(50), new Double(120)
                        },
                        {
                                new Double(75), new Double(80), new Double(150)
                        }
                };
        metaClass.setProperty(dymmyClass, "matrix", matrix2);
        metaClass.setProperty(dymmyClass, "matrix2", matrix2);
    }

    public void testSetPropertyWithArray() {
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
                new Integer(0), new Integer(1), new Integer(2), new Integer(3)
        };
        metaClass.setProperty(dymmyClass, "integers", integers);
        assertEquals(integers, metaClass.getProperty(dymmyClass, "integers"));
    }

    public void testSetPropertyWithList() {
        DymmyClass dymmyClass = new DymmyClass();
        MetaClass metaClass = InvokerHelper.getMetaClass(dymmyClass);

        // test list
        ArrayList list = new ArrayList();
        list.add(new Integer(120));
        list.add(new Integer(150));

        // test int[]
        metaClass.setProperty(dymmyClass, "ints", list);

        // test Integer[]
        metaClass.setProperty(dymmyClass, "integers", list);
    }

    public void testMetaMethodsOnlyAddedOnce() {
        MetaClass metaClass = InvokerHelper.getMetaClass("some String");

        List methods = metaClass.getMetaMethods();
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MetaMethod method = (MetaMethod) iter.next();
            int count = 0;
            for (Iterator inner = methods.iterator(); inner.hasNext();) {
                MetaMethod runner = (MetaMethod) inner.next();
                if (method.equals(runner)) {
                    System.out.println("runner = " + runner);
                    System.out.println("method = " + method);
                    count++;
                }
            }
            assertEquals("count of Method " + method.getName(), 1, count);
        }

    }


    public void doSomething() {
        System.out.println("Called doSomething()");
    }
}


class DymmyClass {
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

}

