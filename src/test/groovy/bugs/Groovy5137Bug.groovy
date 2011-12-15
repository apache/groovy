package groovy.bugs

class Groovy5137Bug extends GroovyTestCase {
    void testShouldNotThrowGroovyBugError() {
        assertScript '''
        import groovy.mock.interceptor.MockFor

        MockFor.getInstance(URLConnection, new URL('http://foo'))

        '''
    }

    void testStaticMethodInCallToSuper() {
        assertScript '''
            class Top { Top(int x) { } }
            class Bottom extends Top {
                Bottom(args) {
                    super(value(*args))
                }
                static int value(x,y) { x+y }
            }
            new Bottom([1,2])
        '''
    }
}
