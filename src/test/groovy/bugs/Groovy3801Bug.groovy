package groovy.bugs

class Groovy3801Bug extends GroovyTestCase {
    void testMainMethodSignature() {
        def gcl = new GroovyClassLoader()
        def clazz

        clazz = gcl.parseClass( """
            class Groovy3801A {
                static main(args) {}
            }
        """
        )
        def stdMainMethod = clazz.getMethods().find {it.name == 'main'}
        assert stdMainMethod.returnType.toString().contains('void')

        clazz = gcl.parseClass( """
            class Groovy3801B {
                static def main(args) {}
            }
        """
        )
        stdMainMethod = clazz.getMethods().find {it.name == 'main'}
        assert stdMainMethod.returnType.toString().contains('void')

        clazz = gcl.parseClass( """
            class Groovy3801C {
                static main() {}
            }
        """
        )
        def nonStdMainMethod = clazz.getMethods().find {it.name == 'main'}
        assert nonStdMainMethod.returnType.toString().contains('java.lang.Object')
    }
}
