/**
 * @author Chris Poirier
 * @version $Revision$
 */
class MethodDispatchBug extends GroovyTestCase {
    void doit(Object parameter1, Object parameter2) {
        System.out.println("TestChild::doit( Object, Object )");
    }

    void doit(Boolean parameter1, Object parameter2) {
        System.out.println("TestChild::doit( Boolean, Object )");
    }

    void doit(Object parameter1, Boolean parameter2) {
        System.out.println("TestChild::doit( Object, Boolean )");
    }

    void doit(Boolean parameter1, Boolean parameter2) {
        System.out.println("TestChild::doit( Boolean, Boolean )");
    }

    void testBug() {
    /* @todo
    	strange - this works fine inside eclipse but fails inside Maven
    	
        o = this;

        System.out.println("Calling Test.doit( Boolean, Boolean ) -- expect Boolean, Boolean");
        o.doit(true, true);

        System.out.println("");
        System.out.println("Calling Test.doit( Boolean, Integer ) -- expect Boolean, Object");
        o.doit(true, 9);

        System.out.println("");
        System.out.println("Calling Test.doit( Integer, Boolean ) -- expect Object, Boolean");
        o.doit(9, true);

        System.out.println("");
        System.out.println("Calling Test.doit( Integer, Integer ) -- expect Object, Object");
        o.doit(9, 9);
    */
    }
}
