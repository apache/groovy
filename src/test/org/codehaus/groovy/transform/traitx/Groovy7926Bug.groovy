package org.codehaus.groovy.transform.traitx

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase
import org.codehaus.groovy.classgen.asm.InstructionSequence

/**
 * Created by graemerocher on 08/09/2016.
 */
class Groovy7926Bug extends AbstractBytecodeTestCase {

    void testThatVoidTypesFromTraitsWithGenericsWork() {
        assertScript('''
trait MyTrait<D> {
    void delete() {
        // no-op
        println "works"
    }
}
class MyImpl implements MyTrait<MyImpl> {
}
new MyImpl().delete()
return true
''')
    }

    void testThatVoidTypesAreNotUsedForVariableNamesInByteCode() {
        def byteCode = compile([method:"delete", classNamePattern:"MyImpl"],"""\
trait MyTrait<D> {
    void delete() {
        // no-op
        println "works"
    }
}
class MyImpl implements MyTrait<MyImpl> {
}
        """)

        def instructions = byteCode.instructions
        byteCode.instructions = instructions[
                instructions.indexOf("public delete()V")..-1
        ]
        instructions = byteCode.instructions
        byteCode.instructions = instructions[
                0..instructions.indexOf( instructions.find { it == '--BEGIN----END--' } )
        ]
        assert !byteCode.hasSequence([
                "CHECKCAST void"
        ])
    }
}
