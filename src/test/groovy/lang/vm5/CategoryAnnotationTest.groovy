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
                String getSuperName1() { 
                    String myname = "" 
                    return myname + "hi from category" 
                }
                // 2nd test case of JIRA
                String getSuperName2() { 
                    String myname = this.getName() 
                    for(int i = 0; i < 5; i++) myname += i 
                    return myname + "-Post"
                }
                // 3rd test case of JIRA
                String getSuperName3() { 
                    String myname = this.getName() 
                    for(i in 0..4) myname += i
                    return myname + "-Post"
                }
            }
    
            interface Test { 
                String getName() 
            }
    
            class MyTest implements Test {
                String getName() {
                    return "Pre-"
                }
            }
    
            def onetest = new MyTest()
            use(TestCategory) { 
                assert onetest.getSuperName1() == "hi from category"
                assert onetest.getSuperName2() == "Pre-01234-Post"
                assert onetest.getSuperName3() == "Pre-01234-Post"
            }
        """
    }

    void testPropertyNameExpandingToGetterInsideCategoryMethod() {
        //GROOVY-3543: Inside the category method, this.getType().name was failng but this.getType().getName() was not.
        assertScript """
            @Category(Guy)
            class Naming {
                String getTypeName() {
                    if(this.getType() != null)
                        this.getType().name
                    else
                        ""
                }
            }
            
            interface Guy {
                Type getType()
            }
            
            class Type {
                String name
            }
            
            class MyGuyver implements Guy {
                Type type
            }
            
            def atype = new Type(name: 'String')
            def onetest = new MyGuyver(type:atype)
            
            use(Naming) {
                assert onetest.getTypeName() == onetest.getType().getName()
            }
        """
    }
}

