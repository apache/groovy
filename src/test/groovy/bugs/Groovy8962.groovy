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

import junit.framework.TestCase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

final class Groovy8962 {

    @Test
    void testShouldCompileProperly_memStub() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'A.groovy')
            a.write '''
                class A {
                    Map<String, Map<String, Integer[]>> columnsMap = [:]
                }
            '''

            def b = new File(parentDir, 'B.java')
            b.write '''
                public class B {
                    public void f(A a) {
                        System.out.println(a.getColumnsMap());
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    @Test
    void testShouldCompileProperly2_memStub() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'A.groovy')
            a.write '''
                class A {
                    Map<String, Map<String, List<Integer[]>[]>> columnsMap = [:]
                }
            '''

            def b = new File(parentDir, 'B.java')
            b.write '''
                public class B {
                    public void f(A a) {
                        System.out.println(a.getColumnsMap());
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    @Test
    void testShouldCompileProperly3_memStub() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'A.groovy')
            a.write '''
                package x
                import y.B
                class A {
                    Map<String, Map<String, List<Integer[]>[]>> columnsMap = [:]
                    B b = null
                }
            '''

            def b = new File(parentDir, 'B.java')
            b.write '''
                package y;
                import x.A;
                public class B {
                    public void f(A a) {
                        System.out.println(a.getColumnsMap());
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    @Test
    void testShouldAllowConstantInSwitch_memStub() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
        try {
            def b = new File(parentDir, 'B.java')
            b.write '''
                public class B {
                    public static void main(String...args) {
                        int x = 4;
                        switch (x) {
                            case groovy.bugs.Groovy8962.Constants.constant: x=1;
                        }
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            addToClassPath(loader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(b)
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    @Test
    void testShouldAllowConstantInSwitchWithStubs_memStub() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
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
            cu.addSources(a, b)
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    @Test
    void testShouldAllowCharConstantInSwitchWithoutStubs_memStub() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
        try {
            def b = new File(parentDir, 'B.java')
            b.write '''
                public class B {
                    public static void main(String...args) {
                        char x = 'z';
                        switch (x) {
                            case groovy.bugs.Groovy8962.Constants.FOOCHAR: x='y';
                        }
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            config.setClasspathList([getClasspathElement(this.class), getClasspathElement(GroovyTestCase), getClasspathElement(TestCase)])
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(b)
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    @Test
    void testShouldAllowCharConstantInSwitchWithStubs_memStub() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
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
            cu.addSources(a, b)
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    @Test
    void testAccessConstantStringFromJavaClass_memStub() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
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
            cu.addSources(a, b)
            cu.compile()

            Class clazz = loader.loadClass("B")
            clazz.newInstance().main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    //--------------------------------------------------------------------------

    private static void addToClassPath(GroovyClassLoader loader) {
        loader.addURL(this.protectionDomain.codeSource.location)
        loader.addURL(GroovyTestCase.protectionDomain.codeSource.location)
        loader.addURL(TestCase.protectionDomain.codeSource.location)
    }

    private static String getClasspathElement(Class c) {
        def codeSource = c.protectionDomain.codeSource
        new File(codeSource.location.toURI()).path.toString()
    }

    static class Constants {
        public static final int constant = 2
        public static final char FOOCHAR = 'x'
    }
}
