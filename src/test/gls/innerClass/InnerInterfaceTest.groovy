package gls.innerClass

/**
 * Tests on inner interface usage
 *
 * @author Roshan Dawrani
 */
class InnerInterfaceTest extends GroovyTestCase {

    void testStaticInnerInterfaceInAClass() {
        assertScript """
            public class Foo4422V1 {
                static public class Bar {
                    def bar(){}
                }
                static public interface Baz {
                    String TEST = ""
                    def baz()
                }
            }
            
            class BazImpl implements Foo4422V1.Baz {
                def baz(){}
            }
            assert Foo4422V1.Bar != null
            assert Foo4422V1.Baz != null
            assert Foo4422V1.Bar.getMethod('bar') != null
            assert Foo4422V1.Baz.getMethod('baz') != null
            assert Foo4422V1.Baz.getField('TEST') != null
            assert BazImpl != null
        """
    }

    void testStaticInnerInterfaceInAnInterface() {
        assertScript """
            public interface Foo4422V2 {
                static public interface Baz {}
            }
            
            assert Foo4422V2.Baz != null
        """
    }
    
    void testNonStaticInnerInterfaceInAClass() {
        assertScript """
            public class Foo4422V3 {
                public class Bar {}
                public interface Baz {}
            }
            
            assert Foo4422V3.Bar != null
            assert Foo4422V3.Baz != null
        """
    }
}