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
package org.codehaus.groovy.classgen

import groovy.test.GroovyTestCase
import org.codehaus.groovy.tools.*
import org.codehaus.groovy.control.*

class GenericsGenTest extends GroovyTestCase {

    void testCompile() {
        File dir = createTempDir("groovy-src-", "-src")
        assertNotNull dir

        def fileList = [
                "JavaClass.java": """
         import java.util.*;

         public abstract class JavaClass<T> implements GroovyInterface<T> {
         }
    """,

                "JavaInterface.java": """
         public interface JavaInterface<X,Y> {
             public X getKey ();
             public Y getValue ();
         }
    """,

                "GroovyInterface.groovy": """
         interface GroovyInterface<X> {
             X method ();
         }
    """,

                "GroovyClass.groovy": """
           class GroovyClass extends JavaClass<String> implements JavaInterface<Long,Boolean> {
               String method() {}
               Long getKey(){}
               Boolean getValue() {}
           }
      """
        ].collect {
            name, text ->
            File file = new File(dir, name)
            file.write text
            file
        }

        CompilerConfiguration config = new CompilerConfiguration()
        config.targetDirectory = createTempDir("groovy-target-", "-target")
        config.jointCompilationOptions = [
                "stubDir": createTempDir("groovy-stub-", "-stub"),
                "namedValues": ["target", "1.7", "source", "1.7"] as String[]
        ]
        config.classpath = "target/classes"
        FileSystemCompiler compiler = new FileSystemCompiler(config)
        compiler.compile(fileList.toArray(new File[fileList.size()]))
    }

    private filesToDelete = []

    void tearDown() {
        filesToDelete.each {file ->
            if (file instanceof File) {
                // remember: null instanceof anything is false
                FileSystemCompiler.deleteRecursive file
            }
        }
    }

    private File createTempDir(prefix, suffix) {
        File tempFile = File.createTempDir(prefix, suffix)
        tempFile.deleteOnExit()
        filesToDelete.add(tempFile)
        return tempFile
    }
}
