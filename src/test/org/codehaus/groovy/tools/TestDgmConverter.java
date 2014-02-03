package org.codehaus.groovy.tools;

import groovy.lang.MetaMethod;
import junit.framework.TestCase;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;

public class TestDgmConverter extends TestCase {

    private static final String REFERENCE_CLASS = "/org/codehaus/groovy/runtime/DefaultGroovyMethods.class";

    public void testConverter () throws URISyntaxException {
        File dgmClassDirectory = new File(TestDgmConverter.class.getResource(REFERENCE_CLASS).toURI()).getParentFile();

        final File[] files = dgmClassDirectory.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            public int compare(final File o1, final File o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            }
        });
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            final String name = file.getName();
            if (name.startsWith("dgm$")) {
                System.out.println(name);

                final String className = "org.codehaus.groovy.runtime." + name.substring(0, name.length() - ".class".length());
                try {
                    Class cls = Class.forName(className, false, DefaultGroovyMethods.class.getClassLoader());
                    Constructor[] declaredConstructors = cls.getDeclaredConstructors();
                    assertEquals(1, declaredConstructors.length);
                    Constructor constructor = declaredConstructors[0];
                    final MetaMethod metaMethod = (MetaMethod) constructor.newInstance(null,null, null, null);
                } catch (ClassNotFoundException e) {
                    fail("Failed to load " + className);
                } catch (IllegalAccessException e) {
                    fail("Failed to instantiate " + className);
                } catch (InstantiationException e) {
                    fail("Failed to instantiate " + className);
                } catch (InvocationTargetException e) {
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
