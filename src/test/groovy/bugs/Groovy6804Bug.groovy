package groovy.bugs

import groovy.transform.NotYetImplemented
import groovy.transform.stc.StaticTypeCheckingTestCase

class Groovy6804Bug extends StaticTypeCheckingTestCase {

    void testOverloadedMethod() {
        assertScript '''
            class Base<K extends Serializable, V> {
                void delete(K key) {}
                
                void delete(V value) {}
            }

            class Foo extends Base<String, Integer> {}

            public class Class1 {
                Class1() {
                    Foo foo = new Foo();
                    
                    foo.delete(1);
                }
            }
            new Class1();
        '''
    }

}
