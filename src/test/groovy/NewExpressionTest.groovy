package groovy

import org.codehaus.groovy.runtime.DummyBean

class NewExpressionTest extends GroovyTestCase {

    void testNewInstance() {
        def cheese = new String( "hey you hosers" )
        
        assert cheese != null
        
        println(cheese)
    }

    void testNewBeanNoArgs() {
        def bean = new DummyBean()
        assert bean.name == "James"
        assert bean.i == 123
    }

    void testNewBean1Args() {
        def bean = new DummyBean("Bob")
        assert bean.name == "Bob"
        assert bean.i == 123
    }

    void testNewBean2Args() {
        def bean = new DummyBean("Bob", 1707)
        assert bean.name == "Bob"
        assert bean.i == 1707
    }

    void testNewInstanceWithFullyQualifiedName() {
        def bean = new org.codehaus.groovy.runtime.DummyBean("Bob", 1707)
        assert bean.name == "Bob"
        assert bean.i == 1707
    }

    void testNewInstanceWithFullyQualifiedNameNotImported() {
        def bean = new java.io.File("Foo")

        println "Created $bean"

        assert bean != null
    }
    
    void testNewOnMultipleLines() {
        def bean = 
          new
            File
            ("Foo")

        assert bean != null
    }

}
