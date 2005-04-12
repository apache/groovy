class PrintTest extends GroovyTestCase {

    /*
    void testToString() {
        assertToString("hello", 'hello')
        
        assertToString([], "[]")
        assertToString([1, 2, "hello"], '[1, 2, hello]')
        
        assertToString([1:20, 2:40, 3:'cheese'], '[1=20, 2=40, 3=cheese]')
        assertToString([:], "[:]")

        assertToString([['bob':'drools', 'james':'geronimo']], '[[james:geronimo, bob:drools]]')
        assertToString([5, ["bob", "james"], ["bob":"drools", "james":"geronimo"], "cheese"], '[5, [bob, james], [james:geronimo, bob:drools], cheese]')
    }
    */

    void testInspect() {
        assertInspect("hello", '"hello"')
        
        assertInspect([], "[]")
        assertInspect([1, 2, "hello"], '[1, 2, "hello"]')
        
        assertInspect([1:20, 2:40, 3:'cheese'], '[1:20, 2:40, 3:"cheese"]')
        assertInspect([:], "[:]")

        assertInspect([['bob':'drools', 'james':'geronimo']], '[["james":"geronimo", "bob":"drools"]]')
        assertInspect([5, ["bob", "james"], ["bob":"drools", "james":"geronimo"], "cheese"], '[5, ["bob", "james"], ["james":"geronimo", "bob":"drools"], "cheese"]')
    }
    
    void testCPlusPlusStylePrinting() {
        endl = "\n"
        
        System.out << "Hello world!" << endl
    }
}