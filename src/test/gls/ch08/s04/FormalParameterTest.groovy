package gls.ch08.s04

import gls.scope.CompilableTestSupport

/**
* a formal parameter is a parameter to a method, this parameter must work
* as any local variable. But we generally do boxing on local variables, which
* is not possible for formal parameters. The type is givven through the
* method signature.
*/
class FormalParameterTest extends CompilableTestSupport{
    
  void testPrimitiveParameterAssignment(){
    // test int and long as they have different lengths on in the bytecode
    assert intMethod(1i,2i) == 2i
    assert longMethod(1l,2l) == 2l
    
  }
  
  int intMethod(int i, int j) {
    i=j
    return i
  }
  
  long longMethod(long i, long j) {
    i=j
    return i
  }
}