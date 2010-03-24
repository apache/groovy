package groovy.bugs

class Groovy4006Bug extends GroovyTestCase {
    void testOuterThisReferenceImplicitPassingToInnerClassConstructorNoArg() {
    	try {
	        assertScript """
				class MyOuterClass4006V1 {
					def outerName = 'OC1' 
					def foo() {
						def ic = new MyInnerClass4006V1()
						ic.bar()
					}
					class MyInnerClass4006V1 {
						def innerName
						MyInnerClass4006V1() {
							this.innerName = 'IC1'
						}
						def bar() {
							assert this.innerName == 'IC1'
							assert this.outerName == 'OC1'
							this.outerName = 'OC1New'
							assert this.outerName == 'OC1New'
							throw new RuntimeException('V1 - Inner class now successfully refers to implicitly passed outer this reference!')
						}
					}
				}
				def oc = new MyOuterClass4006V1()
				oc.foo()
	        """
	        fail('The script run should have failed with RuntimeException, coming from bar() of inner class')
    	} catch (RuntimeException ex) {
    		assert ex.message == 'V1 - Inner class now successfully refers to implicitly passed outer this reference!'
    	}
    }

    void testOuterThisReferenceImplicitPassingToInnerClassConstructorWithArg() {
    	try {
	        assertScript """
				class MyOuterClass4006V2 {
					def outerName = 'OC2' 
					def foo() {
						def ic = new MyInnerClass4006V1('IC2')
						ic.bar()
					}
					class MyInnerClass4006V1 {
						def innerName
						MyInnerClass4006V1(innerName) {
							this.innerName = innerName
						}
						def bar() {
							assert this.innerName == 'IC2'
							assert this.outerName == 'OC2'
							this.outerName = 'OC2New'
							assert this.outerName == 'OC2New'
							throw new RuntimeException('V2 - Inner class now successfully refers to implicitly passed outer this reference!')
						}
					}
				}
				def oc = new MyOuterClass4006V2()
				oc.foo()
	        """
	        fail('The script run should have failed with RuntimeException, coming from bar() of inner class')
    	} catch (RuntimeException ex) {
    		assert ex.message == 'V2 - Inner class now successfully refers to implicitly passed outer this reference!'
    	}
    }

    void testOuterThisReferenceImplicitPassingToInnerClassConstructorWithArgInAProp() {
    	try {
	        assertScript """
				class MyOuterClass4006V3 {
					def outerName = 'OC3' 
					def icField = new MyInnerClass4006V3('IC3');
					def foo() {
						icField.bar()
					}
					class MyInnerClass4006V3 {
						def innerName
						MyInnerClass4006V3(innerName) {
							this.innerName = innerName
						}
						def bar() {
							assert this.innerName == 'IC3'
							assert this.outerName == 'OC3'
							this.outerName = 'OC3New'
							assert this.outerName == 'OC3New'
							throw new RuntimeException('V3 - Inner class now successfully refers to implicitly passed outer this reference!')
						}
					}
				}
				def oc = new MyOuterClass4006V3()
				oc.foo()
	        """
	        fail('The script run should have failed with RuntimeException, coming from bar() of inner class')
    	} catch (RuntimeException ex) {
    		assert ex.message == 'V3 - Inner class now successfully refers to implicitly passed outer this reference!'
    	}
    }

    void testOuterThisReferenceImplicitPassingToInnerClassConstructorWithArgInAField() {
    	try {
	        assertScript """
				class MyOuterClass4006V4 {
					def outerName = 'OC4' 
					private def icField = new MyInnerClass4006V4('IC4');
					def foo() {
						icField.bar()
					}
					class MyInnerClass4006V4 {
						def innerName
						MyInnerClass4006V4(innerName) {
							this.innerName = innerName
						}
						def bar() {
							assert this.innerName == 'IC4'
							assert this.outerName == 'OC4'
							this.outerName = 'OC4New'
							assert this.outerName == 'OC4New'
							throw new RuntimeException('V4 - Inner class now successfully refers to implicitly passed outer this reference!')
						}
					}
				}
				def oc = new MyOuterClass4006V4()
				oc.foo()
	        """
	        fail('The script run should have failed with RuntimeException, coming from bar() of inner class')
    	} catch (RuntimeException ex) {
    		assert ex.message == 'V4 - Inner class now successfully refers to implicitly passed outer this reference!'
    	}
    }

    void testOuterThisReferenceImplicitPassingToInnerClassConstructorWithArgInAProperty() {
    	try {
	        assertScript """
				class MyOuterClass4006V5 {
					def outerName = 'OC5'
					def icProp = new MyInnerClass4006V5();
					def foo() {
						icProp.bar()
					}
					class MyInnerClass4006V5 {
						MyInnerClass4006V5() {
						}
						def bar() {
							assert this.outerName == 'OC5'
							this.outerName = 'OC5New'
							assert this.outerName == 'OC5New'
							throw new RuntimeException('V5 - Inner class now successfully refers to implicitly passed outer this reference!')
						}
					}
				}
				def oc = new MyOuterClass4006V5()
				oc.foo()
	        """
	        fail('The script run should have failed with RuntimeException, coming from bar() of inner class')
    	} catch (RuntimeException ex) {
    		assert ex.message == 'V5 - Inner class now successfully refers to implicitly passed outer this reference!'
    	}
    }
}
