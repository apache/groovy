package com.xseagullx.groovy.gsoc

import spock.lang.Specification

class MainTest extends Specification {
    def "test arguments handling"() {
        // TODO ask someone for help. IDK how to test arguments without global mocking as described here.
        // http://spock-framework.readthedocs.org/en/latest/interaction_based_testing.html#mocking-constructors
    }

    def "test process method"() {
        expect:
        def sourceFile = new File("test_res/com/xseagullx/groovy/gsoc/TestClass1.groovy")

        def moduleNodeOld = new Main(Configuration.OLD).process(sourceFile)
        def moduleNodeNew = new Main(Configuration.NEW).process(sourceFile)
        assert moduleNodeOld && moduleNodeNew
        assert moduleNodeOld.packageName == moduleNodeNew.packageName
        assert moduleNodeOld.imports.collect { [it.alias, it.className] } == moduleNodeNew.imports.collect { [it.alias, it.className] }
        assert moduleNodeOld.classes.collect { [it.name, it.modifiers, it.superClass.name] } == moduleNodeNew.classes.collect { [it.name, it.modifiers, it.superClass.name] }
    }

    def "test class file creation"() {
        expect:
        def sourceFile = new File("test_res/com/xseagullx/groovy/gsoc/TestClass1.groovy")


        def main = new Main(Configuration.NEW)
        main.compile(sourceFile)
    }
}
