package groovy

class SwitchTest extends GroovyTestCase {

    void testSwitch() {
        callSwitch("foo", "foo")
        callSwitch("bar", "barfoo")
        callSwitch("barbar", "barfoo")
        callSwitch("dummy", "d*")
        callSwitch("xyz", "xyzDefault")
        callSwitch("zzz", "Default")
        callSwitch(4, "List")
        callSwitch(5, "List")
        callSwitch(6, "List")
        callSwitch("inList", "List")
        callSwitch(1, "Integer")
        callSwitch(1.2, "Number")
        callSwitch(null, "null")
        callSwitch([1,2,3], "ListInterface")
    }

    def callSwitch(x, expected) {
        println("Calling switch with ${x}")

        def result = ""

        switch (x) {
        	case null:
        		result = "null"
                break

        	case ~/d.*/:
        		result = "d*"
                break

            case "barbar":
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
                
            case List:
                result = "ListInterface"
                break

            case "xyz":
                result = result + "xyz"

            default:
                result = result + "Default"

                // unnecessary just testing compiler
                break;
        }
        println("Found result ${result}")

        assert result == expected , "when calling switch with ${x}"
    }
    
    // test the continue in switch, which should jump to the the while start
    void testSwitchScope() {
        def i = 0
        def j = 0
        while (true) {
            ++i;
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

    void testSwitchWithClosure(){
        switch(0){
            case {true}: break
            default: assert false
        }
        switch(0){
            case {false}: assert false
        }
        switch(0){
            case {it == 0}: break
            default: assert false
        }
        switch(0){
            case { candidate -> candidate == 0}: break
            default: assert false
        }
    }
    
    /** older versions of groovy produced a ListExpression for a
        fall through. the result was that it worked in some cases
        and in other cases not. For example not for patterns **/
    void testFallthrough() {
      def a = [ 'FileName.java', 'AnotherFileName.groovy' ]
      def i = 0
      a.each {
        switch( it ) {
  	      case ~/.*java$/:
  	      case ~/.*groovy$/:
  	        i++
            break;
  	      default:
	        assert false
	    }
      }
      assert i==2
    }
}
