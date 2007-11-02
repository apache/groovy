package groovy

class FinallyTest extends GroovyTestCase{
 
  void testBreakInTry() {
    def called = false
    while (true){
      try {
        break
      } finally {
        called = true
      }
    }
    assert called, "finally block was not called"
  }
  
  void testBreakInFinally() {
    def called = false
    while (true){
      try {
        throw new Exception("foo")
      } catch (e) {
        assert e.message == "foo"
      } finally {
        called = true
        break
      }
    }
    assert called, "finally block was not called"
  }
  
  void testContinueInTry() {
    def called = false
    boolean b = true
    while (b){
      try {
        b=false
        continue
      } finally {
        called = true
      }
    }
    assert called, "finally block was not called"
  }
  
  void testContinueInFinally() {
    def called = false
    boolean b = true
    while (b){
      try {
        throw new Exception("foo")
      } catch (e) {
        assert e.message == "foo"
      } finally {
        b=false
        called = true
        continue
      }
    }
    assert called, "finally block was not called"
  }
  
  void testReturn() {
    def map = methodWithReturnInTry()
    assert map.called, "finally block was not called"
    def called = methodWithReturnInFinally()
    assert called, "finally block was not called"
  }
  
  def methodWithReturnInTry(){
    def map = [:]
    try {
      return map
    } finally {
	  map.called = true
    }
  }
  
  def methodWithReturnInFinally(){
    try {
      return false
    } finally {
	  return true
    }
  }
  
  void testStackeFinally(){
    def calls = methodWithStackedFinally()
    if (calls==12) {
      assert false,"wrong order of finally blocks"
    }
    assert calls==102 
  }

  def methodWithStackedFinally(){
    def calls = 0
    def first = true;
    try {
      try {
        calls = 0
      } finally {
        calls++
        if (first) {
          first = false
        } else {
          calls += 10
        }
      }
    } finally {
      calls++
      if (first) {
        first = false
      } else {
        calls += 100
      }
    }
    return calls
  }
  
  def multipleReturn() {
    try { 
        if (0 == 1) return 1
        return 2
    }
    finally { 
        return 3
    }
  }
  
  void testMultipleReturn(){
    assert multipleReturn() == 3
  }
}