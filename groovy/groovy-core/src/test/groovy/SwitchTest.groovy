class SwitchTest extends GroovyTestCase {

    void testSwitch() {
        callSwitch("foo", "foo")
        callSwitch("bar", "barfoo")
        callSwitch("xyz", "xyzDefault")
        callSwitch("zzz", "Default")
    }
    
    callSwitch(x, expected) {
		println("Calling switch with ${x}")
		
		result = ""
		
        switch (x) {
            case "bar":
	            result = result + "bar"
                
            case "foo":
    	        result = result + "foo"
                break
                
            case "xyz":
        	    result = result + "xyz"
                
            default:
                result = result + "Default"
                
                // unnecessary just testing compiler
                break;
        }
        println("Found result ${result}")
        
        assert result == expected : "when calling switch with ${x}"
    }
}
