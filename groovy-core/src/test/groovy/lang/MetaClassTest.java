/*
 * $Id$
 * 
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
 */

package groovy.lang;

import groovy.util.GroovyTestCase;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
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

