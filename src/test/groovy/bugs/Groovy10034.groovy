package groovy.bugs

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

final class Groovy10034 extends AbstractBytecodeTestCase {
    void testObjectArrayParam() {
        def result = compile method:'test', '''
            @groovy.transform.CompileStatic
            def test() {
                ["x"].toArray(new String[0])
            }
        '''
        int offset = result.indexOf('ANEWARRAY java/lang/String', result.indexOf('--BEGIN--'))
        assert result.hasStrictSequence(['ANEWARRAY java/lang/String','INVOKEINTERFACE java/util/List.toArray'], offset)
        // there should be no 'INVOKEDYNAMIC cast' instruction here: ^
    }
}
