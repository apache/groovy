package org.codehaus.groovy.tools;

import groovy.lang.MetaMethod;
import junit.framework.TestCase;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;

import java.io.File;

public class TestDgmConverter extends TestCase {

    public void testConverter () {
        final File[] files = new File("target/classes/org/codehaus/groovy/runtime").listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            final String name = file.getName();
            if (name.startsWith("dgm$")) {
                System.out.println(name);

                final String className = "org.codehaus.groovy.runtime." + name.substring(0, name.length() - ".class".length());
                try {
                    Class cls = Class.forName(className);
                    final MetaMethod metaMethod = (MetaMethod) cls.newInstance();
                    System.out.println(metaMethod);
                } catch (ClassNotFoundException e) {
                    fail("Failed to load " + className);
                } catch (IllegalAccessException e) {
                    fail("Failed to instantiate " + className);
                } catch (InstantiationException e) {
                    fail("Failed to instantiate " + className);
                }
            }
        }
    }

    public void testRegistry () {
        final MetaClassRegistryImpl metaClassRegistry = new MetaClassRegistryImpl();
        final Object [] instanceMethods = metaClassRegistry.getInstanceMethods().getArray();
        for (int i = 0; i < instanceMethods.length; i++) {
            System.out.println(instanceMethods[i]);

        }
    }
}
