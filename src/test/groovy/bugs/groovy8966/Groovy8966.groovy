package groovy.bugs.groovy8966

import groovy.test.GroovyTestCase
import groovy.transform.CompileStatic
import org.junit.Test

@CompileStatic
final class Groovy8966 extends GroovyTestCase {
    @Test
    void test() {
        assertScript '''
            def array = [0, 1, 2]
            int a = 2 
            long b = 3
            assert array[a..<b] == [2]
        '''
    }
}