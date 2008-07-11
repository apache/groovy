package groovy.bugs

class Groovy2949Bug extends GroovyTestCase {
    void testBug () {
        new GroovyShell().evaluate """
        public abstract class A { abstract protected void doIt() }

        class B extends A {
           void doIt() {}
        }

        new ProxyGenerator(debug:true).instantiateDelegateWithBaseClass([ x : { int a, int b -> } ], [], new B())
        """
    }
}