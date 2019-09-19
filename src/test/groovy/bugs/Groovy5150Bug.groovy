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

import groovy.test.GroovyTestCase
import junit.framework.TestCase
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit

class Groovy5150Bug extends GroovyTestCase {
    static class Constants {
        public static final int constant = 2
        public static final char FOOCHAR = 'x'
    }
    void testShouldAllowConstantInSwitch() {
        def config = new CompilerConfiguration()
        config.with {
            targetDirectory = createTempDir()
            jointCompilationOptions = [stubDir: createTempDir()]
        }

        File parentDir = createTempDir()
        try {
            def b = new File(parentDir, 'B.java')
            b.write '''
            public class B {
                public static void main(String...args) {
                    int x = 4;
                    switch (x) {
                        case groovy.bugs.Groovy5150Bug.Constants.constant: x=1;
                    }
                }
            }
        '''
            def loader = new GroovyClassLoader(this.class.classLoader)
            addToClassPath(loader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources([b] as File[])
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
            config.jointCompilationOptions.stubDir.deleteDir()
        }
    }

    void testShouldAllowConstantInSwitchWithStubs() {
        def config = new CompilerConfiguration()
        config.with {
            targetDirectory = createTempDir()
            jointCompilationOptions = [stubDir: createTempDir()]
        }

        File parentDir = createTempDir()
        try {
            def a = new File(parentDir, 'A.groovy')
            a.write '''
                class A {
                    public static final int constant = 1
                }
            '''
            def b = new File(parentDir, 'B.java')
            b.write '''
            public class B {
                public static void main(String...args) {
                    int x = 4;
                    switch (x) {
                        case A.constant: x=1;
                    }
                }
            }
        '''
            def loader = new GroovyClassLoader(this.class.classLoader)
            addToClassPath(loader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources([a,b] as File[])
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
            config.jointCompilationOptions.stubDir.deleteDir()
        }
    }

    static addToClassPath(GroovyClassLoader loader) {
        loader.addURL(this.getProtectionDomain().getCodeSource().getLocation())
        loader.addURL(GroovyTestCase.class.getProtectionDomain().getCodeSource().getLocation())
        loader.addURL(TestCase.class.getProtectionDomain().getCodeSource().getLocation())
    }

    void testShouldAllowCharConstantInSwitchWithoutStubs() {
        def config = new CompilerConfiguration()
        config.with {
            targetDirectory = createTempDir()
            jointCompilationOptions = [stubDir: createTempDir()]
        }

        File parentDir = createTempDir()
        try {
            def b = new File(parentDir, 'B.java')
            b.write '''
            public class B {
                public static void main(String...args) {
                    char x = 'z';
                    switch (x) {
                        case groovy.bugs.Groovy5150Bug.Constants.FOOCHAR: x='y';
                    }
                }
            }
        '''
            def loader = new GroovyClassLoader(this.class.classLoader)
            config.setClasspathList ([getClasspathElement(this.class), getClasspathElement(GroovyTestCase),  getClasspathElement(TestCase)])
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources([b] as File[])
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
            config.jointCompilationOptions.stubDir.deleteDir()
        }
    }

    private static getClasspathElement(Class c) {
        def codeSource = c.protectionDomain.codeSource
        def file = new File(codeSource.getLocation().toURI()).getPath()
        return file.toString()
    }

    void testShouldAllowCharConstantInSwitchWithStubs() {
        def config = new CompilerConfiguration()
        config.with {
            targetDirectory = createTempDir()
            jointCompilationOptions = [stubDir: createTempDir()]
        }

        File parentDir = createTempDir()
        try {
            def a = new File(parentDir, 'A.groovy')
            a.write '''
                class A {
                    public static final char constant = 'x'
                }
            '''
            def b = new File(parentDir, 'B.java')
            b.write '''
            public class B {
                public static void main(String...args) {
                    char x = 'z';
                    switch (x) {
                        case A.constant: x='y';
                    }
                }
            }
        '''
            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources([a,b] as File[])
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
            config.jointCompilationOptions.stubDir.deleteDir()
        }
    }

    void testAccessConstantStringFromJavaClass() {
        def config = new CompilerConfiguration()
        config.with {
            targetDirectory = createTempDir()
            jointCompilationOptions = [stubDir: createTempDir()]
        }

        File parentDir = createTempDir()
        try {
            def a = new File(parentDir, 'A.groovy')
            a.write '''
                class A {
                    public static final String CONSTANT = "hello, world!"
                }
            '''
            def b = new File(parentDir, 'B.java')
            b.write '''
            public class B {
                public static void main(String...args) {
                    if (!"hello, world!".equals(A.CONSTANT)) throw new RuntimeException("Constant should not be: ["+A.CONSTANT+"]");
                }
            }
        '''
            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources([a,b] as File[])
            cu.compile()
            Class clazz = loader.loadClass("B")
            clazz.newInstance().main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
            config.jointCompilationOptions.stubDir.deleteDir()
        }
    }

    private static File createTempDir() {
        File.createTempDir("groovyTest${System.currentTimeMillis()}", "")
    }
}
