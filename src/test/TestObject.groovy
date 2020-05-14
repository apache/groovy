

import org.codehaus.groovy.runtime.NullObject

class TestObject {
    void func(def v) {
        throw new IllegalAccessException("Should not call me!")
    }
    void func1(def v) {
        throw new IllegalAccessException("Should not call me!")
    }
    void func1(NullObject v) {}
}
