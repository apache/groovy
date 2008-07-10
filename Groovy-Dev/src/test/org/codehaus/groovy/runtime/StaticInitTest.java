package org.codehaus.groovy.runtime;

import groovy.lang.GroovyClassLoader;
import junit.framework.TestCase;

import java.lang.reflect.Field;

class X {

    public static int field = 0;

    static {
        StaticInitTest.failed = true;
        System.out.println("INIT");
    }
}

public class StaticInitTest extends TestCase {

    static boolean failed;

    public void testInitOrder () throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        System.out.println("GET FIELD");
        final Field f = new GroovyClassLoader().loadClass("org.codehaus.groovy.runtime.X", false, false, false).getField("field");
        System.out.println(failed);
        assertTrue(!failed);
        f.getInt(null);
        System.out.println(failed);
        assertTrue(failed);
    }
}
