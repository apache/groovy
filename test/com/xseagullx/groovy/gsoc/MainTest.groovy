
package com.xseagullx.groovy.gsoc

import com.xseagullx.groovy.gsoc.util.ASTComparatorCategory
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.control.ErrorCollector
import spock.lang.Specification

class MainTest extends Specification {

    def "test ast builder"() {
        setup:
        def file = new File(path)
        def moduleNodeNew = new Main(Configuration.NEW).process(file)
        def moduleNodeOld = new Main(Configuration.OLD).process(file)
        def moduleNodeOld2 = new Main(Configuration.OLD).process(file)
        config = config.is(_) ? ASTComparatorCategory.DEFAULT_CONFIGURATION : config

        expect:
        ASTComparatorCategory.apply(config) {
            assert moduleNodeOld == moduleNodeOld2
        }

        ASTComparatorCategory.apply(config) {
            assert moduleNodeNew == moduleNodeOld, "Fail in $path"
        }

        where:
        path | config
        "test_res/com/xseagullx/groovy/gsoc/Annotations_Issue30_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/Annotations_Issue30_2.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/ArrayType_Issue44_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/AssignmentOps_Issue23_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/ClassConstructorBug_Issue13_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/ClassInitializers_Issue_20_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/ClassMembers_Issue3_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/ClassMembers_Issue3_2.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/ClassModifiers_Issue_2.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/ClassProperty_Issue4_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/Closure_Issue21_1.groovy" | addIgnore(ExpressionStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "test_res/com/xseagullx/groovy/gsoc/Enums_Issue43_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/ExceptionHandling_Issue27_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/Extendsimplements_Issue25_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/FieldAccessAndMethodCalls_Issue37_1.groovy" | _
        'test_res/com/xseagullx/groovy/gsoc/FieldAccessAndMethodCalls_Issue37_2.groovy' | addIgnore(ExpressionStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "test_res/com/xseagullx/groovy/gsoc/FieldInitializersAndDefaultMethods_Issue49_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/Generics_Issue26_1.groovy" | addIgnore(GenericsType, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "test_res/com/xseagullx/groovy/gsoc/GStrings_Issue41_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/ImportRecognition_Issue6_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/ImportRecognition_Issue6_2.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/InnerClasses_Issue48_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/ListsAndMaps_Issue22_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/Literals_Numbers_Issue36_1.groovy" | _
        'test_res/com/xseagullx/groovy/gsoc/Literals_Other_Issue36_4.groovy' | _
        "test_res/com/xseagullx/groovy/gsoc/Literals_HexOctNumbers_Issue36_2.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/Literals_Strings_Issue36_3.groovy" | addIgnore(ExpressionStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "test_res/com/xseagullx/groovy/gsoc/MapParameters_Issue55.groovy" | addIgnore(ExpressionStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "test_res/com/xseagullx/groovy/gsoc/MemberAccess_Issue14_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/MethodBody_Issue7_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/MethodCall_Issue15_1.groovy" | addIgnore(ExpressionStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "test_res/com/xseagullx/groovy/gsoc/New_Issue47_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/Operators_Issue9_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/ParenthesisExpression_Issue24_1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/Script_Issue50_1.groovy" | addIgnore(ExpressionStatement, ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "test_res/com/xseagullx/groovy/gsoc/Statements_Issue17_1.groovy" | addIgnore([IfStatement, ExpressionStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "test_res/com/xseagullx/groovy/gsoc/Statements_Issue58_1.groovy" | addIgnore([IfStatement, ExpressionStatement], ASTComparatorCategory.LOCATION_IGNORE_LIST)
        "test_res/com/xseagullx/groovy/gsoc/TernaryAndElvis_Issue57.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/TestClass1.groovy" | _
        "test_res/com/xseagullx/groovy/gsoc/ThrowDeclarations_Issue_28_1.groovy" | _
    }

    def addIgnore(Class aClass, ArrayList<String> ignore, Map<Class, List<String>> c = null) {
        c = c ?: ASTComparatorCategory.DEFAULT_CONFIGURATION.clone() as Map<Class, List<String>>;
        c[aClass].addAll(ignore)
        c
    }

    def addIgnore(Collection<Class> aClass, ArrayList<String> ignore, Map<Class, List<String>> c = null) {
        c = c ?: ASTComparatorCategory.DEFAULT_CONFIGURATION.clone() as Map<Class, List<String>>;
        aClass.each { c[it].addAll(ignore) }
        c
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
}
