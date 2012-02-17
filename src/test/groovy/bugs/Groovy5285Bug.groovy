package groovy.bugs

class Groovy5285Bug extends GroovyTestCase {
    void testShouldNotThrowStackOverflow() {
        assertScript '''
            class Test {
                void setMetaClass(MetaClass metaClass) {
                    super.setMetaClass(metaClass)
                }
            }

            def obj = new Test()
            obj.metaClass = obj.metaClass
        '''
    }
}
