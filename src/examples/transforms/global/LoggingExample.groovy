package transforms.global

/**
* Demonstrates how a global transformation works. 
* 
* @author Hamlet D'Arcy
*/ 

def greet() {
    println "Hello World"
}
    
// this prints out Hello World along with the extra compile time logging
greet()


//
// The rest of this script is asserting that this all works correctly. 
//

// redirect standard out so we can make assertions on it
def standardOut = new ByteArrayOutputStream();
System.setOut(new PrintStream(standardOut)); 
  
greet()
def result = standardOut.toString("ISO-8859-1").split('\n')
assert "Starting greet"  == result[0].trim()
assert "Hello World"     == result[1].trim()
assert "Ending greet"    == result[2].trim()
standardOut.close()
