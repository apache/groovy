class SwitchTest extends GroovyTestCase {

    void testSwitch() {
        callSwitch("foo", "foo")
        callSwitch("bar", "barfoo")
        callSwitch("xyz", "xyzDefault")
        callSwitch("zzz", "Default")
        callSwitch(4, "List")
        callSwitch(5, "List")
        callSwitch(6, "List")
        callSwitch("inList", "List")
        callSwitch(1, "Integer")
        callSwitch(1.2, "Number")
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

            case [4, 5, 6, 'inList']:
                result = "List"
                break
                
            case Integer:
                result = "Integer"
                break
                
            case Number:
                result = "Number"
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

    // test the continue in switch, which should jump to the the while start
    void testSwitchScope() {
        i = 0
        j = 0
        while (true) {
            i++;
            switch(i) {
                case 4:
                    continue
                case 5:
                    break;
                default:
                    j += i;
                    break;
            }
            if (i == 5) break;
        }
        assert j == 6
    }
    
}
