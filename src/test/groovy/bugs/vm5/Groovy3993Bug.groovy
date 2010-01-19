package groovy.bugs.vm5

class Groovy3993Bug extends GroovyTestCase {
    void testClassToRunOrder() {
        try {
            // Before this fix, instead of running the outer TestInnerEnum3993 class, inner TestInnerEnum3993$MyEnum class was being run 
            new GroovyShell().run """
                class TestInnerEnum3993 {
                      
                    static enum MyEnum {
                        a, b, c
                        static MyEnum[] myenums = [a,b,c]
                    }
                  
                    static main(args) {
                        assert MyEnum.a.name() == 'a'
                        assert MyEnum.myenums.length == 3
                        assert MyEnum.myenums[0].name() == 'a'
                        throw new RuntimeException('Top level class TestInnerEnum3993 successfully run, and not the enum class')
                    }
                }
            """, 'TestInnerEnum3993.groovy', []
            fail('Should have failed with a RuntimeException')
        } catch (RuntimeException ex) {
            assert ex.message == 'Top level class TestInnerEnum3993 successfully run, and not the enum class'
        }
    }
}
