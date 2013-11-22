package groovy.bugs

class Groovy6374Bug extends GroovyTestCase {
    void testShouldNotAllowCoercionOfFinalClass() {
        assertScript '''import org.codehaus.groovy.runtime.typehandling.GroovyCastException

final class Foo {}
try {
    [bar: { -> }] as Foo
} catch (GroovyCastException e) {
    assert e.message == 'Cannot coerce a map to class Foo because it is a final class'
}
'''
    }
}
