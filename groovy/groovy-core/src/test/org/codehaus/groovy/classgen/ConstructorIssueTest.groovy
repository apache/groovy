package org.codehaus.groovy.classgen

class ConstructorIssueTest extends GroovyTestCase {
    
    ConstructorIssueTest() {
        println("Created test case!")
    }
    
    static void main(args) {
        System.out.println("About to create test");
        foo = new ConstructorIssueTest()
        foo.done()
        System.out.println("Done");
    }
    
    void done() {
        println("Yeah, I've been made")
    }
    
    void testConstructorIssue() {
        main([null].toArray())
    }
}
