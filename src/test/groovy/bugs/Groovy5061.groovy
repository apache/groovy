package groovy.bugs

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit

class Groovy5061 extends GroovyTestCase {
    void testShouldCompileProperly() {
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
            cu.addSources([a, b] as File[])
            cu.compile()
        } finally {
            parentDir.deleteDir()
        }

    }

    void testShouldCompileProperly2() {
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
            cu.addSources([a, b] as File[])
            cu.compile()
        } finally {
            parentDir.deleteDir()
        }

    }

    private static File createTempDir() {
        File.createTempDir("groovyTest${System.currentTimeMillis()}", "")
    }
}
