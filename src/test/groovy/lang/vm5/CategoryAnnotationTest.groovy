package groovy.lang.vm5

class CategoryAnnotationTest extends GroovyTestCase {
    void testTransformationOfPropertyInvokedOnThis() {
    	//Test the fix for GROOVY-3367
        assertScript """
            @Category(Distance3367)
            class DistanceCategory3367 {
                Distance3367 plus(Distance3367 increment) {
                    new Distance3367(number: this.number + increment.number)
                }
            }
    
            class Distance3367 {
                def number
            }
    
            use(DistanceCategory3367) {
                def d1 = new Distance3367(number: 5)
                def d2 = new Distance3367(number: 10)
                def d3 = d1 + d2
                assert d3.number == 15
            }
        """
    }
	
    void testCategoryMethodsHavingDeclarationStatements() {
        // GROOVY-3543: Declaration statements in category class' methods were not being visited by 
        // CategoryASTTransformation's expressionTransformer resulting in declaration variables not being 
        // defined on varStack resulting in compilation errors later
        assertScript """
            @Category(Test)
            class TestCategory {
                String getSuperName() { 
                    String myname = "" // 
                    return myname + "hi from category" 
                }
            }
    
            interface Test { 
                String getName() 
            }
    
            class MyTest implements Test {
                String getName() {
                    return "hi"
                }
            }
    
            def onetest = new MyTest()
            assert onetest.getName() == "hi"
            use(TestCategory) { 
                assert onetest.getSuperName() == "hi from category" 
            }
        """
    }
}

