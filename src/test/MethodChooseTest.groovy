

import groovy.test.GroovyTestCase
import org.codehaus.groovy.runtime.NullObject

class MethodChooseTest extends GroovyTestCase {
    void testShouldChooseNullVersion() {
        assert new TestObject().func(null) == NullObject.class
    }
    void testShouldChooseNullVersion2() {
        new TestObject().func1(null)
    }
}
