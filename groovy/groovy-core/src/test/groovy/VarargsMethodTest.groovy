class VarargsMethodTest extends GroovyTestCase {

    void testVarargsOnly() {
        // assertEquals 0, varargsOnlyMethod() // todo: fix. throws NPE
        assertEquals 1, varargsOnlyMethod('')
        assertEquals 1, varargsOnlyMethod(1)
        assertEquals 2, varargsOnlyMethod('','')
        assertEquals 1, varargsOnlyMethod( ['',''] )
        assertEquals 2, varargsOnlyMethod( *['',''] )
    }

    Integer varargsOnlyMethod(Object[] args) {
        return args.size()
    }

    void testVarargsLast() {
        assertEquals 0, varargsLastMethod('')
        assertEquals 0, varargsLastMethod(1)
        assertEquals 1, varargsLastMethod('','')
        assertEquals 2, varargsLastMethod('','','')
        assertEquals 1, varargsLastMethod('', ['',''] )
        assertEquals 2, varargsLastMethod('', *['',''] )
    }

    Integer varargsLastMethod(Object first, Object[] args) {
        return args.size()
    }
}
