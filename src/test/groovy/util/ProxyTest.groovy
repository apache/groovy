package groovy.util

/**
* @author Dierk Koenig
**/
class ProxyTest extends GroovyTestCase {

    void testStringDecoration(){
        original = 'decorated String'
        proxy = new StringDecorator().wrap(original)
        // method, that is only known on proxy
        assertSame original, proxy.adaptee
        // method, that is only known on adaptee is relayed through the proxy
        assertEquals original.size(), proxy.size()
        // method, that is availabe in both objects should come from proxy
        assertEquals 0, proxy.length()
        // method, that is availabe in both objects
        // but should come from adaptee needs explicit relay
        assertEquals original, proxy.toString()
        // method from decorator, that is not in adaptee
        assertEquals 'new Method reached', proxy.someNewMethod()
    }
}

class StringDecorator extends Proxy{
    int length()          { 0 }
    String toString()     { adaptee.toString()}
    String someNewMethod(){ 'new Method reached' }
}
