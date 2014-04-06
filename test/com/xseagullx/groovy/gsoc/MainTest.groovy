package com.xseagullx.groovy.gsoc

import com.xseagullx.groovy.gsoc.util.ASTComparatorCategory
import spock.lang.Specification

class MainTest extends Specification {
    def "test arguments handling"() {
        // TODO ask someone for help. IDK how to test arguments without global mocking as described here.
        // http://spock-framework.readthedocs.org/en/latest/interaction_based_testing.html#mocking-constructors
    }

    def "test process method"() {
        setup:
        def sourceFile = new File("test_res/com/xseagullx/groovy/gsoc/TestClass1.groovy")

        def moduleNodeNew = new Main(Configuration.NEW).process(sourceFile)
        def moduleNodeOld = new Main(Configuration.OLD).process(sourceFile)
        def moduleNodeOld2 = new Main(Configuration.OLD).process(sourceFile)

        expect:
        use(ASTComparatorCategory) {
            assert moduleNodeOld == moduleNodeOld2;
            assert moduleNodeNew == moduleNodeOld
            true
        }
    }

    def "test class file creation"() {
        expect:
        def sourceFile = new File("test_res/com/xseagullx/groovy/gsoc/TestClass1.groovy")

        def main = new Main(Configuration.NEW)
        main.compile(sourceFile)
    }
}
