package org.codehaus.syntax.parser

class UnknownClass {
    
    main() {
        try {
        	println("Hello World!")
        }
        catch (UnknownException e) {
            println("This will never happen")
        }
    }
}
