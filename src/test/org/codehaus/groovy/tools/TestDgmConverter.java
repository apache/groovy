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

public class TestDgmConverter extends TestCase {

    private static final String REFERENCE_CLASS = "/org/codehaus/groovy/runtime/dgm$0.class";

    public void testConverter () throws URISyntaxException {
        File dgmClassDirectory = new File(TestDgmConverter.class.getResource(REFERENCE_CLASS).toURI()).getParentFile();

        final File[] files = dgmClassDirectory.listFiles();
        Arrays.sort(files, (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName()));
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            final String name = file.getName();
            if (name.startsWith("dgm$")) {
                final String className = "org.codehaus.groovy.runtime." + name.substring(0, name.length() - ".class".length());
                try {
                    Class cls = Class.forName(className, false, DefaultGroovyMethods.class.getClassLoader());
                    Constructor[] declaredConstructors = cls.getDeclaredConstructors();
                    assertEquals(1, declaredConstructors.length);
                    Constructor constructor = declaredConstructors[0];
                    final MetaMethod metaMethod = (MetaMethod) constructor.newInstance(null,null, null, null);
                } catch (ClassNotFoundException e) {
                    fail("Failed to load " + className);
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    fail("Failed to instantiate " + className);
                }
            }
        }
    }

    public void testRegistry () {
        final MetaClassRegistryImpl metaClassRegistry = new MetaClassRegistryImpl();
        final Object [] instanceMethods = metaClassRegistry.getInstanceMethods().getArray();
        assertTrue(instanceMethods.length > 0);
    }
}
