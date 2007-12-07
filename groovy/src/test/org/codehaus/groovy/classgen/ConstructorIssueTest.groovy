package org.codehaus.groovy.classgen

import groovy.bugs.TestSupport

class ConstructorIssueTest extends TestSupport {
    
    ConstructorIssueTest() {
        //println("Created test case!")
    }

    ConstructorIssueTest(String[] args) {
        //println("Created test case!")
    }

    static void main(args) {
        //println("in main() - called with ${array}")
        
        def foo = new ConstructorIssueTest()
        foo.done()

        //System.out.println("Done");
    }
    
    void done() {
        println("Yeah, I've been made")
    }
    
    void testConstructorIssue() {
        def array = getMockArguments()

        main(array)

        new ConstructorIssueTest(array).done()
    }
}
