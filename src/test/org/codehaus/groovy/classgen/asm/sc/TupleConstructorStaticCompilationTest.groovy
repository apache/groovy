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

    void testTupleConstructorWithMissingArgumentOfSameTypeAsPrevious() {
        assertScript '''
            @groovy.transform.TupleConstructor
            class Person {
                String firstName
                String lastName
                Integer age
                Integer priority
            }

            @groovy.transform.CompileStatic
            Person m() {
                new Person('Cedric','Champeau',32)
            }
            def p = m()
            assert p.firstName == 'Cedric'
            assert p.lastName == 'Champeau'
            assert p.age == 32
            assert p.priority == null
        '''
    }
    
    void testConstructorWithDefaultArgsAndPossibleMessup() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                String val
                Foo(String arg1='foo', String arg2) {
                    arg1+arg2
                }
            }
            new Foo('bar').val == 'foobar'
        '''
    }
}
