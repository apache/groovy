package org.codehaus.syntax.parser

class UnknownClass {
    
    main() {
        try {
        	"Hello World!".println()
        }
        catch (UnknownException e) {
            "This will never happen".println()
        }
    }
}
