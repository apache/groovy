package gls.invocation

import gls.scope.CompilableTestSupport

public class ClosureDelegationTest extends CompilableTestSupport {

    public void testMissingMethodMissingMethod() {
      assertScript """
class A {
  def methodMissing(String name, args) {
     "A" 
  }
}

def methodMissing(String name, args) {
  visited=true
  throw new MissingMethodException(name,this.class,args)
}

visited=false
def closure = { foo() }
closure.delegate = new A()
assert closure() == "A"
assert visited==true
        """
    }

    public void testInvokeMethodMissingMethod() {
      assertScript """
class A {
  def invokeMethod(String name, args) {
     "A" 
  }
}

def methodMissing(String name, args) {
  visited=true
  throw new MissingMethodException(name,this.class,args)
}

visited=false
def closure = { foo() }
closure.delegate = new A()
assert closure() == "A"
assert visited==true
        """
    }
    
    public void testMissingMethodInvokeMethod() {
      assertScript """
class A {
  def methodMissing(String name, args) {
     "A" 
  }
}

def invokeMethod(String name, args) {
  visited=true
  throw new MissingMethodException(name,this.class,args)
}

visited=false
def closure = { foo() }
closure.delegate = new A()
assert closure() == "A"
assert visited==true
        """
    }
    
    public void testInvokeMethodInvokeMethod() {
      assertScript """
class A {
  def invokeMethod(String name, args) {
     "A" 
  }
}

def invokeMethod(String name, args) {
  visited=true
  throw new MissingMethodException(name,this.class,args)
}

visited=false
def closure = { foo() }
closure.delegate = new A()
assert closure() == "A"
assert visited==true
        """
    }
}