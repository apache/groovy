package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

class TupleConstructorStaticCompilationTest extends AbstractBytecodeTestCase {
    void testTupleConstructor1() {
        assertScript '''
            @groovy.transform.TupleConstructor
            class Person {
                String firstName
                String lastName
            }

            @groovy.transform.CompileStatic
            Person m() {
                new Person('Cedric','Champeau')
            }
            def p = m()
            assert p.firstName == 'Cedric'
            assert p.lastName == 'Champeau'
        '''
    }

    void testTupleConstructor1WithMissingArgument() {
        assertScript '''
            @groovy.transform.TupleConstructor
            class Person {
                String firstName
                String lastName
                Integer age
            }

            @groovy.transform.CompileStatic
            Person m() {
                new Person('Cedric','Champeau')
            }
            def p = m()
            assert p.firstName == 'Cedric'
            assert p.lastName == 'Champeau'
            assert p.age == null
        '''
    }
}
