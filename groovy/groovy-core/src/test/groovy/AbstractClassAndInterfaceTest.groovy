class AbstractClassAndInterfaceTest extends GroovyTestCase {

	void testInterface() {
    	def shell = new GroovyShell()
        def text = """
        	interface A {
				void methodOne(Object o)
				Object methodTwo()
			}
			
			class B implements A {
				void methodOne(Object o){assert true}
				Object methodTwo(){
					assert true
					methodOne(null)
					return new Object()
				}
			}
			
			def b = new B();
			return b.methodTwo()
			"""
		def retVal = shell.evaluate(text)
		assert retVal.class == Object
	}
	
	void testClassImplementingAnInterfaceButMissesMethod() {
    	def shell = new GroovyShell()
        def text = """
        	interface A {
				void methodOne(Object o)
				Object methodTwo()
			}
			
			class B implements A {
				void methodOne(Object o){assert true}
			}
			
			def b = new B();
			return b.methodTwo()
			"""
		shouldFail {
			shell.evaluate(text)
		}
		text = """
			interface A {
				Object methodTwo()
		    }
        	interface B extends A{
				void methodOne(Object o)
			}
			
			class C implements A {
				void methodOne(Object o){assert true}
			}
			
			def b = new C();
			return b.methodTwo()
			"""
		shouldFail {
			shell.evaluate(text)
		}			
	}
	
	void testAbstractClass() {
    	def shell = new GroovyShell()
        def text = """
        	abstract class A {
				abstract void methodOne(Object o)
				Object methodTwo(){
					assert true
					methodOne(null)
					return new Object()
				}
			}
			
			class B extends A {
				void methodOne(Object o){assert true}
			}
			
			def b = new B();
			return b.methodTwo()
			"""
		def retVal = shell.evaluate(text)
		assert retVal.class == Object
	}	
	
	void testClassExtendingAnAbstractClassButMissesMethod() {
    	def shell = new GroovyShell()
        def text = """
        	abstract class A {
				abstract void methodOne(Object o)
				Object methodTwo(){
					assert true
					methodOne(null)
					return new Object()
				}
				abstract void MethodThree()
			}
			
			abstract class B extends A {
				void methodOne(Object o){assert true}
			}
			
			class C extends B{}
			
			def b = new C();
			return b.methodTwo()
			"""
		shouldFail {
			shell.evaluate(text)
		}			
		
        text = """
        	abstract class A {
				abstract void methodOne(Object o)
				Object methodTwo(){
					assert true
					methodOne(null)
					return new Object()
				}
				abstract void MethodThree()
			}
			
			class B extends A {
				void methodOne(Object o){assert true}
			}
			
			def b = new B();
			return b.methodTwo()
			"""
		shouldFail {
			shell.evaluate(text)
		}
	}
	
	void testInterfaceAbstractClassCombination() {
    	def shell = new GroovyShell()
        def text = """
			interface A {
				void methodOne()
			}
			
			abstract class B implements A{
				abstract void methodTwo()
			}
			
			class C extends B {
				void methodOne(){assert true}
				void methodTwo(){
				  methodOne()
				}
			}
			def c = new C()
			c.methodTwo()
			"""
		shell.evaluate(text)
		
		text = """
			interface A {
				void methodOne()
			}
			
			abstract class B implements A{
				abstract void methodTwo()
			}
			
			class C extends B {}
			def c = new c()
			c.methodTwo()
			"""
		shouldFail {
			shell.evaluate(text)
		}	
	}
}