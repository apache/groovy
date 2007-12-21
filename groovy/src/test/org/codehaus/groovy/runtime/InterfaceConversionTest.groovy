package org.codehaus.groovy.runtime;

class InterfaceConversionTest extends GroovyTestCase {
 
  void testClosureConversion(){
	def c1 = {Object[] args -> args?.length}
	def c2 = c1 as InterfaceConversionTestFoo
	assert !(c1 instanceof InterfaceConversionTestFoo)
	assert c2 instanceof InterfaceConversionTestFoo
	assert c2.a() == null
	assert c2.b(null) == 1
  }
  
  void testMapConversion() {  
	def m1 = [a:{1}, b:{2}]
	def m2 = m1 as InterfaceConversionTestFoo
	
	assert !(m1 instanceof InterfaceConversionTestFoo)
	assert m2 instanceof InterfaceConversionTestFoo
	assert m2.a() == 1
	assert m2.b(null) == 2
  }
}
 
interface InterfaceConversionTestFoo {
    def a();
    def b(Integer i);
}