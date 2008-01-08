package org.codehaus.groovy.classgen

class  CastTest extends GroovyTestCase {
    void testCast () {
        new GroovyShell ().parse(new File("src/test/org/codehaus/groovy/benchmarks/alioth/binarytrees.groovy"))
    }
}