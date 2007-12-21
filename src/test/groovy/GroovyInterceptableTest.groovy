package groovy

import org.codehaus.groovy.runtime.ReflectionMethodInvoker

class GroovyInterceptableTest extends GroovyTestCase {

    void testMethodInterception() {
        def g = new GI()
        assert g.someInt() == 2806
        assert g.someUnexistingMethod() == 1
        assert g.toString() == "invokeMethodToString"
    }

    void testProperties() {
        def g = new GI()
        assert g.foo == 89
        g.foo = 90
        assert g.foo == 90
        // should this be 1 or 90?
        assert g.getFoo() == 1
    }
    
    void testCallMissingMethod() {
        def obj = new GI2()
        shouldFail { obj.notAMethod() }
        assert 'missing' == obj.result 
    }
 
    void testCallMissingMethodFromInstance() {
        def obj = new GI2()
        shouldFail { obj.method() }
        assert 'missing' == obj.result
   }
}

class GI implements GroovyInterceptable {

    def foo = 89

    int someInt() { 2806 }
    String toString() { "originalToString" }

    Object invokeMethod(String name, Object args) {
        if ("toString" == name)
            return "invokeMethodToString"
        else if ("someInt" == name)
            return ReflectionMethodInvoker.invoke(this, name, args)
        else
            return 1
    }
}


class GI2 implements GroovyInterceptable {
  def result = ""
  def invokeMethod(String name, args) {
    def metaMethod = Foo.metaClass.getMetaMethod(name, args)
    if (metaMethod != null) return metaMethod.invoke(this, args)
    result += "missing"
    throw new MissingMethodException(name, Foo.class, args)
  }
  
  def method() {
      notAMethod()
  }
}
