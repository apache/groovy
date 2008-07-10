package org.codehaus.groovy.runtime;

import groovy.bugs.TestSupport

class StaticPrintlnTest extends TestSupport {

    void testStaticPrint() {
        main(getMockArguments())
	}
	
    static void main(args) {
        println("called with: " + args)
    }
}