package groovy.bugs

import java.util.logging.Level

class Groovy2432Bug extends GroovyLogTestCase {

    void testMe () {
        withLevel (Level.ALL, "methodCalls.groovy.bugs.Groovy2432Bug.println") {
            withLevel (Level.ALL, MetaClass.class.getName()) {
                println new WillCauseInfiniteLoop().toString()
            }
        }
    }
}

class WillCauseInfiniteLoop {

    String toString() {
        def buffer = new StringBuffer()
        buffer.leftShift(this.getClass().getName())
        buffer.leftShift('@')
        buffer.leftShift(hashCode())
        return buffer.toString()
    }
}
