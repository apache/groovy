class SwitchTest extends GroovyTestCase {

    void testSwitch() {
        x = "foo"
        
        switch (x) {
         
            case "bar":
                println("bar")
                
            case "foo":
                println("foo")
                
            default:
                println("Default")
        }
    }
}
