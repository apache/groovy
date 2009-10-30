package groovy.bugs.vm5

import gls.CompilableTestSupport

class Groovy3852Bug extends CompilableTestSupport {
    def gcl = new GroovyClassLoader()
    void testDuplicationAnnotationOnClassNoParams() {
        try {
            gcl.parseClass """
                @Deprecated
                @Deprecated
                @Deprecated
                class TooDeprecatedGroovy3852V1 {}
            """
            fail('The class compilation should have failed as it has duplication annotations')
        }catch(ex) {
            assertTrue ex.message.contains('Cannot specify duplicate annotation')
        }
    }

    void testDuplicationAnnotationOnClassWithParams() {
        try {
            gcl.parseClass """
                @Newify(auto=false, value=Integer)
                @Newify(auto=false, value=Integer)
                @Newify(auto=false, value=Integer)
                class TooDeprecatedGroovy3852V2 {}
            """
            fail('The class compilation should have failed as it has duplication annotations')
        }catch(ex) {
            assertTrue ex.message.contains('Cannot specify duplicate annotation')
        }
    }

    void testDuplicationAnnotationOnOtherTargets() {
        try {
            gcl.parseClass """
                class TooDeprecatedGroovy3852V3 {
                    @Deprecated
                    @Deprecated
                    @Deprecated
                    def m() {}
                }
            """
            fail('The class compilation should have failed as it has duplication annotations on a method')
        }catch(ex) {
            assertTrue ex.message.contains('Cannot specify duplicate annotation')
        }

        try {
            gcl.parseClass """
                class TooDeprecatedGroovy3852V3 {
                    @Deprecated
                    @Deprecated
                    @Deprecated
                    def f
                }
            """
            fail('The class compilation should have failed as it has duplication annotations on a field')
        }catch(ex) {
            assertTrue ex.message.contains('Cannot specify duplicate annotation')
        }
    }
}