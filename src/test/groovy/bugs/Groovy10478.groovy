package groovy.bugs

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy10478 {

    @Test
    void testIndirectInterface() {
        assertScript '''
            trait A {
                final String string = 'works'
            }
            interface B {
            }
            trait C implements A, B {
            }
            @groovy.transform.CompileStatic
            class D implements C {
            }

            // VerifyError: Bad invokespecial instruction: interface method reference is in an indirect superinterface
            //    Location: D.Atrait$super$getString()Ljava/lang/String; @37: invokespecial
            def cls = D.class
            cls.name

            String result = new D().string
            assert result == 'works'
        '''
    }
}
