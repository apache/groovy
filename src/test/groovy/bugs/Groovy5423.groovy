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
package groovy.bugs

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

final class Groovy5423 {
    @Test
    void testStaticImportVersusSuperClassMethod() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )
        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'p').mkdir()

            def a = new File(parentDir, 'p/Utils.java')
            a.write '''
                package p;
                public class Utils {
                    public static String getValue() {
                        return "Utils.getValue";
                    }
                }
            '''
            def b = new File(parentDir, 'p/MyBaseClass.java')
            b.write '''
                package p;
                public class MyBaseClass {
                    public static String getValue() {
                        return "MyBaseClass.getValue";
                    }
                }
            '''
            def c = new File(parentDir, 'p/JavaSubClass.java')
            c.write '''
                package p;
                import static p.Utils.*;
                public class JavaSubClass extends MyBaseClass {
                    public String retrieveValue() {
                        return getValue();
                    }
                }
            '''
            def d = new File(parentDir, 'p/GroovySubClass.groovy')
            d.write '''
                package p
                import static p.Utils.*
                class GroovySubClass extends MyBaseClass {
                    String retrieveValue() {
                        getValue()
                    }
                }
            '''
            def e = new File(parentDir, 'p/Main.groovy')
            e.write '''
                package p

                def pojo = new JavaSubClass()
                assert pojo.retrieveValue() == 'MyBaseClass.getValue'

                def pogo = new GroovySubClass()
                assert pogo.retrieveValue() == 'MyBaseClass.getValue'
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c, d, e)
            cu.compile()

            loader.loadClass('p.Main').main()
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }
}
