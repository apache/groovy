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
package org.codehaus.groovy.classgen;

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;

import java.io.File;


/**
 * Tests dynamically compiling a new class
 */
public class GroovyClassLoaderTest extends TestSupport {

    public void testCompile() throws Exception {
        Class groovyClass = loader.parseClass(new File("src/test/org/codehaus/groovy/classgen/Main.groovy"));

        System.out.println("Invoking main...");

        GroovyObject object = (GroovyObject) groovyClass.newInstance();

        assertTrue(object != null);

        MetaClass metaClass = object.getMetaClass();
        System.out.println("Metaclass: " + metaClass);

        Class type = object.getClass();
        System.out.println("Type: " + type);

        // invoke via metaclass
        metaClass.invokeMethod(object, "main", null);

        // invoke directly
        object.invokeMethod("main", null);
    }
}
