package org.codehaus.groovy.classgen

import groovy.bugs.TestSupport

class ConstructorIssueTest extends TestSupport {
    
    ConstructorIssueTest() {
        println("Created test case!")
    }
    
    static void main(args) {
        //println("in main() - called with ${array}")
        
        foo = new ConstructorIssueTest()
        foo.done()

        //System.out.println("Done");
    }
    
    void done() {
        println("Yeah, I've been made")
    }
    
    void testConstructorIssue() {
        array = getMockArguments()
        
        main(array)
    }
}
