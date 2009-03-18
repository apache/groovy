package groovy.bugs

class Groovy3410Bug extends GroovyTestCase {

    void testClassVerificationErrorsWithBooleanExpUsingPrimitiveFields() {
        assertScript """
            class Groovy3405N1 {
                long id // or float or double
                
                boolean bar() {
                    return (id ? true : false)
                }
            }
            println new Groovy3405N1()     
        """

            assertScript """
            class Groovy3405N2 {
                long id
                def bar() {
                    return (id ? "a" : "b")
                }
            }              
            println new Groovy3405N2()     
        """

            assertScript """
            class Groovy3405N3 {
                long id = 0
                def bar() {
                    assert id, "Done"
                }
            }   
            println new Groovy3405N3()     
        """

        assertScript """
            class Groovy3405N4 {
                long id = 0
                def bar() {
                    while(id){
                        print "here"
                        break
                    }
                }
            }   
            println new Groovy3405N4()     
        """

        assertScript """
            class Groovy3405N5 {
                long id = 0
                def bar() {
                    if(id) {
                        true
                    } else {
                        false
                    }
                }
            }   
            println new Groovy3405N5()     
        """
        println "testClassVerificationErrorsWithBooleanExpUsingPrimitiveFields Done" 
    }
}
