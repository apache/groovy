import org.codehaus.groovy.runtime.DummyBean

class NewExpressionTest extends GroovyTestCase {

    void testNewInstance() {
        cheese = new String( "hey you hosers" )
        
        assert cheese != null
        
        cheese.println()
    }

    void testNewBeanNoArgs() {
        bean = new DummyBean()
        assert bean.name == "James"
        assert bean.i == 123
    }

    void testNewBean1Args() {
        bean = new DummyBean("Bob")
        assert bean.name == "Bob"
        assert bean.i == 123
    }

    void testNewBean2Args() {
        bean = new DummyBean("Bob", 1707)
        assert bean.name == "Bob"
        assert bean.i == 1707
    }
}
