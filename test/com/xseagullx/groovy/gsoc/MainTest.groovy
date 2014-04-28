package com.xseagullx.groovy.gsoc
import com.xseagullx.groovy.gsoc.util.ASTComparatorCategory
import org.codehaus.groovy.control.ErrorCollector
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

    def "test class modifiers"() {
        setup:
        def sourceFile = new File("test_res/com/xseagullx/groovy/gsoc/ClassModifiers_Issue_2.groovy")

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

    def "test invalid class modifiers"() {
        expect:
        def file = new File(path)

        def errorCollectorNew = new Main(Configuration.NEW).process(file).context.errorCollector
        def errorCollectorOld = new Main(Configuration.OLD).process(file).context.errorCollector
        def errorCollectorOld2 = new Main(Configuration.OLD).process(file).context.errorCollector


        def cl = { ErrorCollector errorCollector, int it -> def s = new StringWriter(); errorCollector.getError(it).write(new PrintWriter(s)); s.toString() }
        def errOld1 = (0..<errorCollectorOld.errorCount).collect cl.curry(errorCollectorOld)
        def errOld2 = (0..<errorCollectorOld2.errorCount).collect cl.curry(errorCollectorOld2)
        def errNew = (0..<errorCollectorNew.errorCount).collect cl.curry(errorCollectorNew)

        assert errOld1 == errOld2
        assert errOld1 == errNew

        where:
        path | output
        "test_res/com/xseagullx/groovy/gsoc/ClassModifiersInvalid_Issue1_2.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/ClassModifiersInvalid_Issue2_2.groovy" | _
    }

    def "test class members"() {
        expect:
        def file = new File(path)
        def moduleNodeNew = new Main(Configuration.NEW).process(file)
        def moduleNodeOld = new Main(Configuration.OLD).process(file)
        def moduleNodeOld2 = new Main(Configuration.OLD).process(file)

        use(ASTComparatorCategory) {
            assert moduleNodeOld == moduleNodeOld2;
            assert moduleNodeNew == moduleNodeOld
            true
        }

        where:
        path | output
        "test_res/com/xseagullx/groovy/gsoc/class/members/ClassMembers_Issue3_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/class/members/ClassMembers_Issue3_2.groovy" | _
    }

    def "test methods"() {
        expect:
        def file = new File(path)
        def moduleNodeNew = new Main(Configuration.NEW).process(file)
        def moduleNodeOld = new Main(Configuration.OLD).process(file)
        def moduleNodeOld2 = new Main(Configuration.OLD).process(file)

        use(ASTComparatorCategory) {
            assert moduleNodeOld == moduleNodeOld2;
            assert moduleNodeNew == moduleNodeOld
            true
        }

        where:
        path | output
        "test_res/com/xseagullx/groovy/gsoc/class/statements/MethodBody_Issue7_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/class/statements/Operators_Issue9_1.groovy" | _
    }

    def "test properties"() {
        expect:
        def file = new File(path)
        def moduleNodeNew = new Main(Configuration.NEW).process(file)
        def moduleNodeOld = new Main(Configuration.OLD).process(file)
        def moduleNodeOld2 = new Main(Configuration.OLD).process(file)

        use(ASTComparatorCategory) {
            assert moduleNodeOld == moduleNodeOld2;
            assert moduleNodeNew == moduleNodeOld
            true
        }

        where:
        path | output
        "test_res/com/xseagullx/groovy/gsoc/class/properties/ClassProperty_Issue4_1.groovy" | _
    }

    def "test member access"() {
        expect:
        def file = new File(path)
        def moduleNodeNew = new Main(Configuration.NEW).process(file)
        def moduleNodeOld = new Main(Configuration.OLD).process(file)
        def moduleNodeOld2 = new Main(Configuration.OLD).process(file)

        use(ASTComparatorCategory) {
            assert moduleNodeOld == moduleNodeOld2;
            assert moduleNodeNew == moduleNodeOld
            true
        }

        where:
        path | output
        "test_res/com/xseagullx/groovy/gsoc/class/memberAccess/MemberAccess_Issue14_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/class/methodCall/MethodCall_Issue15_1.groovy" | _
    }


    def "test class constructor recognition"() {
        expect:
        def file = new File(path)
        def moduleNodeNew = new Main(Configuration.NEW).process(file)
        def moduleNodeOld = new Main(Configuration.OLD).process(file)
        def moduleNodeOld2 = new Main(Configuration.OLD).process(file)

        use(ASTComparatorCategory) {
            assert moduleNodeOld == moduleNodeOld2;
            assert moduleNodeNew == moduleNodeOld
            true
        }

        where:
        path | output
        "test_res/com/xseagullx/groovy/gsoc/class/members/ClassConstructorBug_Issue13_1.groovy" | _
    }

    def "test import recognition"() {
        expect:
        def file = new File(path)
        def moduleNodeNew = new Main(Configuration.NEW).process(file)
        def moduleNodeOld = new Main(Configuration.OLD).process(file)
        def moduleNodeOld2 = new Main(Configuration.OLD).process(file)

        use(ASTComparatorCategory) {
            assert moduleNodeOld == moduleNodeOld2;
            assert moduleNodeNew == moduleNodeOld
            true
        }

        where:
        path | output
        "test_res/com/xseagullx/groovy/gsoc/class/import/ImportRecognition_Issue6_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/class/import/ImportRecognition_Issue6_2.groovy" | _
    }

    def "test class file creation"() {
        expect:
        def sourceFile = new File("test_res/com/xseagullx/groovy/gsoc/TestClass1.groovy")

        def main = new Main(Configuration.NEW)
        main.compile(sourceFile)
    }
}
