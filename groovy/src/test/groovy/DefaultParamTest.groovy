package groovy

class DefaultParamTest extends GroovyTestCase {

    //GROOVY-2191

    def m2191a(String one, String two = "two") {"$one $two"}
    def m2191a(String one, String two = "two", String three = "three") {"$one $two $three"}
    
    def m2191b(String one, String two = "two", String three = "three") {"$one $two $three"}
    def m2191b(String one, String two = "two") {"$one $two"}
    
    void test2191() {
      assert m2191a("bar") == "bar two"
      assert m2191b("bar") == "bar two three"
    } 

    void testDefaultParameters() {
    
    	def value = doSomething("X", "Y", "Z")
    	assert value == "X-Y-Z"
    	
    	value = doSomething("X", "Y")
    	assert value == "X-Y-defC"
    	
    	value = doSomething("X")
    	assert value == "X-defB-defC"
    	
    	shouldFail { doSomething() }
    }

    void testDefaultTypedParameters() {
    	def value = doTypedSomething("X", "Y", "Z")
    	assert value == "X-Y-Z"
    	
    	value = doTypedSomething("X", "Y")
    	assert value == "X-Y-defC"
    	
    	value = doTypedSomething("X")
    	assert value == "X-defB-defC"
    	
    	value = doTypedSomething()
    	assert value == "defA-defB-defC"
    }

    void testDefaultTypedParametersAnother() {
    	def value = doTypedSomethingAnother("X", "Y", "Z")
    	assert value == "X-Y-Z"
    	
    	value = doTypedSomethingAnother("X", "Z")
    	assert value == "X-defB-Z"
    	
    	value = doTypedSomethingAnother("Z")
    	assert value == "defA-defB-Z"
    	
    	shouldFail{ value = doTypedSomethingAnother() }
    }


    def doSomething(a, b = 'defB', c = 'defC') {
        println "Called with a: ${a}, b ${b}, c ${c}"

        return a + "-" + b + "-" + c
    }

    String doTypedSomething(String a = 'defA', String b = 'defB', String c = 'defC') {
        println "Called typed method with a: ${a}, b ${b}, c ${c}"

        return a + "-" + b + "-" + c
    }

    String doTypedSomethingAnother(String a = 'defA', String b = 'defB', String c) {
        println "Called typed method with a: ${a}, b ${b}, c ${c}"

        return a + "-" + b + "-" + c
    }
    
    void testConstructor() {
        assert DefaultParamTestTestClass.declaredConstructors.size() == 2
        def foo = new DefaultParamTestTestClass()
        assert foo.j == 1
        foo = new DefaultParamTestTestClass(2)
        assert foo.j == 2
    }
}

class DefaultParamTestTestClass {
  def j
  DefaultParamTestTestClass(int i = 1){j=i}
}