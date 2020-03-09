/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.transform.stc

import groovy.test.NotYetImplemented
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.tools.WideningCategories
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.transform.stc.StaticTypesMarker

/**
 * Unit tests for static type checking : type inference.
 */
class TypeInferenceSTCTest extends StaticTypeCheckingTestCase {

    void testStringToInteger() {
        assertScript '''
            def name = "123" // we want type inference
            name.toInteger() // toInteger() is defined by DGM
        '''
    }

    void testGStringMethods() {
        assertScript '''
            def myname = 'Cedric'
            "My upper case name is ${myname.toUpperCase()}"
            println "My upper case name is ${myname}".toUpperCase()
        '''
    }

    void testAnnotationOnSingleMethod() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate '''
            // calling a method which has got some dynamic stuff in it

            import groovy.transform.TypeChecked
            import groovy.xml.MarkupBuilder

            class Greeter {
                @TypeChecked
                String greeting(String name) {
                    generateMarkup(name.toUpperCase())
                }

                // MarkupBuilder is dynamic so we won't do typechecking here
                String generateMarkup(String name) {
                    def sw = new StringWriter()
                    def mkp = new MarkupBuilder()
                    mkp.html {
                        body {
                            div name
                        }
                    }
                    sw
                }
            }

            def g = new Greeter()
            g.greeting("Guillaume")
        '''
    }

    void testInstanceOf() {
        assertScript '''
            Object o
            if (o instanceof String) o.toUpperCase()
        '''
    }

    void testEmbeddedInstanceOf() {
        assertScript '''
            Object o
            if (o instanceof Object) {
                if (o instanceof String) {
                    o.toUpperCase()
                }
            }
        '''
    }

    void testEmbeddedInstanceOf2() {
        assertScript '''
            Object o
            if (o instanceof String) {
                if (true) {
                    o.toUpperCase()
                }
            }
        '''
    }

    void testEmbeddedInstanceOf3() {
        shouldFailWithMessages '''
            Object o
            if (o instanceof String) {
                if (o instanceof Object) { // causes the inferred type of 'o' to be overwritten
                    o.toUpperCase()
                }
            }
        ''', 'Cannot find matching method java.lang.Object#toUpperCase()'
    }

    void testInstanceOfAfterEach() {
        shouldFailWithMessages '''
            Object o
            if (o instanceof String) {
               o.toUpperCase()
            }
            o.toUpperCase() // ensure that type information is lost after if()
        ''', 'Cannot find matching method java.lang.Object#toUpperCase()'
    }

    void testInstanceOfInElseBranch() {
        shouldFailWithMessages '''
            Object o
            if (o instanceof String) {
               o.toUpperCase()
            } else {
                o.toUpperCase() // ensure that type information is lost in else
            }
        ''', 'Cannot find matching method java.lang.Object#toUpperCase()'
    }

    @NotYetImplemented // GROOVY-9454
    void testInstanceOfOnGenericProperty() {
        assertScript '''
            interface Face {
            }
            class Impl implements Face {
                String something
            }
            class Task<R extends Face> implements java.util.concurrent.Callable<String> {
                R request

                @Override
                String call() {
                    if (request instanceof Impl) {
                        def thing = request.something // No such property: something for class: R
                        def lower = thing.toLowerCase()
                    } else {
                        // ...
                        return null
                    }
                }
            }
            def task = new Task<Impl>(request: new Impl(something: 'Hello World'))
            assert task.call() == 'hello world'
        '''
    }

    void testMultipleInstanceOf() {
        assertScript '''
            class A {
               void foo() { println 'ok' }
            }

            class B {
               void foo() { println 'ok' }
               void foo2() { println 'ok 2' }
            }

            def o = new A()

            if (o instanceof A) {
               o.foo()
            }

            if (o instanceof B) {
               o.foo()
            }

            if (o instanceof A || o instanceof B) {
              o.foo()
            }
        '''
    }

    void testInstanceOfInTernaryOp() {
        assertScript '''
            class A {
               int foo() { 1 }
            }
            class B {
               int foo2() { 2 }
            }
            def o = new A()
            int result = o instanceof A?o.foo():(o instanceof B?o.foo2():3)
        '''
    }

    void testShouldNotAllowDynamicVariable() {
        shouldFailWithMessages '''
            String name = 'Guillaume'
            println naamme
        ''', 'The variable [naamme] is undeclared'
    }

    void testInstanceOfInferenceWithImplicitIt() {
        assertScript '''
        ['a', 'b', 'c'].each {
            if (it instanceof String) {
                println it.toUpperCase()
            }
        }
        '''
    }

    void testInstanceOfTypeInferenceWithDef() {
        assertScript '''
            def profile = ['Guillaume', 34, true]
            def item = profile[0]
            if (item instanceof String) {
                println item.toUpperCase()
            }
        '''
    }

    void testInstanceOfTypeInferenceWithoutDef() {
        assertScript '''
            def profile = ['Guillaume', 34, true]
            if (profile[0] instanceof String) {
                println profile[0].toUpperCase()
            }
        '''
    }

    void testInstanceOfInferenceWithField() {
        assertScript '''
            class A {
                int x
            }
            def a
            if (a instanceof A) {
                a.x = 2
            }
        '''
    }

    void testInstanceOfInferenceWithFieldAndAssignment() {
        shouldFailWithMessages '''
            class A {
                int x
            }
            def a = new A()
            if (a instanceof A) {
                a.x = '2'
            }
        ''', 'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testInstanceOfInferenceWithMissingField() {
        shouldFailWithMessages '''
            class A {
                int x
            }
            def a
            if (a instanceof A) {
                a.y = 2
            }
        ''', 'No such property: y for class: A'
    }

    void testShouldNotFailWithWith() {
        assertScript '''
            class A {
                int x
            }
            def a = new A()
            a.with {
                x = 2 // should be recognized as a.x at compile time
            }
            assert a.x == 2
        '''
    }

    void testShouldFailWithWith() {
        shouldFailWithMessages '''
            class A {
                int x
            }
            def a = new A()
            a.with {
                x = '2' // should be recognized as a.x at compile time and fail because of wrong type
            }
        ''', 'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testShouldNotFailWithWithTwoClasses() {
        // we must make sure that type inference engine in this case
        // takes the same property as at runtime
        assertScript '''
            class A {
                int x
            }
            class B {
                String x
            }
            def a = new A()
            def b = new B()
            a.with {
                b.with {
                    x = '2' // should be recognized as b.x at compile time
                }
            }
            assert a.x == 0
            assert b.x == '2'
        '''
    }

    void testShouldNotFailWithWithAndImplicitIt() {
        assertScript '''
            class A {
                int x
            }
            def a = new A()
            a.with {
                it.x = 2 // should be recognized as a.x at compile time
            }
            assert a.x == 2
        '''
    }

    void testShouldNotFailWithWithAndExplicitIt() {
        assertScript '''
            class A {
                int x
            }
            def a = new A()
            a.with { it ->
                it.x = 2 // should be recognized as a.x at compile time
            }
            assert a.x == 2
        '''
    }

    void testShouldNotFailWithWithAndExplicitTypedIt() {
        shouldFailWithMessages '''
            class A {
                int x
            }
            def a = new A()
            a.with { String it ->
                it.x = 2 // should be recognized as a.x at compile time
            }
        ''', 'Expected parameter of type A but got java.lang.String'
    }

    void testShouldNotFailWithInheritanceAndWith() {
         assertScript '''
             class A {
                 int x
                 void method() { println x }
             }
             class B extends A {
             }
             def b = new B()
             b.with {
                 x = 2 // should be recognized as b.x at compile time
             }
             assert b.x == 2
         '''
    }

    void testCallMethodInWithContext() {
        assertScript '''
            class A {
                int method() { return 1 }
            }
            def a = new A()
            a.with {
                method()
            }
        '''
    }

   void testCallMethodInWithContextAndShadowing() {
       // make sure that the method which is found in 'with' is actually the one from class A
       // which returns a String
       assertScript '''
            class A {
                String method() { return 'Cedric' }
            }

            int method() { 1 }

            def a = new A()
            a.with {
                method().toUpperCase()
            }
        '''

       // check that if we switch signatures, it fails
       shouldFailWithMessages '''
            class A {
                int method() { 1 }
            }

            String method() { 'Cedric' }

            def a = new A()
            a.with {
                method().toUpperCase()
            }
        ''', 'Cannot find matching method int#toUpperCase()'
   }

    void testDeclarationTypeInference() {
        MethodNode method
        config.addCompilationCustomizers(new CompilationCustomizer(CompilePhase.CLASS_GENERATION) {
            @Override
            void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
                method = classNode.methods.find { it.name == 'method' }
            }

        })
        assertScript '''
            void method() {
                def o
                o = 1
                o = 'String'
            }
        '''
        def inft = method.code.statements[0].expression.leftExpression.getNodeMetaData(StaticTypesMarker.DECLARATION_INFERRED_TYPE)
        assert inft instanceof WideningCategories.LowestUpperBoundClassNode
        [Comparable, Serializable].each {
            assert ClassHelper.make(it) in inft.interfaces
        }

        assertScript '''
            void method() {
                def o
                o = 1
                o = 2
            }
        '''
        assert method.code.statements[0].expression.leftExpression.getNodeMetaData(StaticTypesMarker.DECLARATION_INFERRED_TYPE) == ClassHelper.int_TYPE

        assertScript '''
            void method() {
                def o
                o = 1L
                o = 2
            }
        '''
        inft = method.code.statements[0].expression.leftExpression.getNodeMetaData(StaticTypesMarker.DECLARATION_INFERRED_TYPE)
        assert inft  == ClassHelper.long_TYPE

        assertScript '''
            void method() {
                def o
                o = new HashSet()
                o = new LinkedHashSet()
            }
        '''
        assert method.code.statements[0].expression.leftExpression.getNodeMetaData(StaticTypesMarker.DECLARATION_INFERRED_TYPE) == ClassHelper.make(HashSet)
    }

    void testChooseMethodWithTypeInference() {
        assertScript '''
            void method(Object o) { println 'Object' }
            void method(int i) { println 'int' }
            def obj = 1
            method(obj)
        '''
    }

    void testStarOperatorOnMap() {
        assertScript '''
            List keys = [x:1,y:2,z:3]*.key
            List values = [x:1,y:2,z:3]*.value
        '''
    }

    void testStarOperatorOnMap2() {
        assertScript '''
            List keys = [x:1,y:2,z:3]*.key
            List values = [x:'1',y:'2',z:'3']*.value
            keys*.toUpperCase()
            values*.toUpperCase()
        '''

        shouldFailWithMessages '''
            List values = [x:1,y:2,z:3]*.value
            values*.toUpperCase()
        ''', 'Cannot find matching method java.lang.Integer#toUpperCase()'
    }

    void testStarOperatorOnMap3() {
        assertScript '''
            def keys = [x:1,y:2,z:3]*.key
            def values = [x:'1',y:'2',z:'3']*.value
            keys*.toUpperCase()
            values*.toUpperCase()
        '''

        shouldFailWithMessages '''
            def values = [x:1,y:2,z:3]*.value
            values*.toUpperCase()
        ''', 'Cannot find matching method java.lang.Integer#toUpperCase()'
    }

    void testFlowTypingWithStringVariable() {
        // as anything can be assigned to a string, flow typing engine
        // could "erase" the type of the original variable although is must not
        assertScript '''
            String str = new Object() // type checker will not complain, anything assignable to a String
            str.toUpperCase() // should not complain
        '''
    }

    void testDefTypeAfterLongThenIntAssignments() {
        assertScript '''
            def o
            o = 1L
            o = 2
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.rightExpression.accessedVariable.getNodeMetaData(DECLARATION_INFERRED_TYPE) == long_TYPE
            })
            def z = o
        '''
    }

    // GROOVY-5519
    void testInferThrowable() {
        assertScript '''
            try {
                throw new RuntimeException('ok')
            } catch (e) {
                handleError(e)
            }
            void handleError(Throwable e) {
                assert e.message == 'ok'
            }
        '''
    }

    void testInferMapValueType() {
        assertScript '''
            Map<String, Integer> map = new HashMap<String,Integer>()
            map['foo'] = 123
            map['bar'] = 246
            Integer foo = map['foo']
            assert foo == 123
            Integer bar = map.get('bar')
            assert bar == 246
        '''
    }

    // GROOVY-5522
    void testTypeInferenceWithArrayAndFind() {
        assertScript '''
            File findFile() {
                new File[0].find { File f -> f.hidden }
            }
            findFile()
        '''
    }

    void testShouldNotThrowIncompatibleArgToFunVerifyError() {
        assertScript '''
            Object convertValueToType(Object value, Class targetType) {
                if (value instanceof CharSequence) {
                    value = value.toString()
                }
                if (value instanceof String) {
                    String strValue = value.trim()
                }
            }
            convertValueToType('foo', String)
        '''
    }

    void testSwitchCaseAnalysis() {
        assertScript '''
            import org.codehaus.groovy.ast.tools.WideningCategories.LowestUpperBoundClassNode as LUB

            def method(int x) {
                def returnValue= new Date()
                switch (x) {
                    case 1:
                        returnValue = 'string'
                        break;
                    case 2:
                        returnValue = 1;
                        break;
                }
                @ASTTest(phase=INSTRUCTION_SELECTION,value={
                    def ift = node.getNodeMetaData(INFERRED_TYPE)
                    assert ift instanceof LUB
                    assert ift.name == 'java.io.Serializable'
                })
                def val = returnValue

                returnValue
            }
        '''
    }

    void testGroovy6215() {
        assertScript '''
            def processNumber(int x) {
                def value = getValueForNumber(x)
                value
            }

            def getValueForNumber(int x) {
                def valueToReturn
                switch(x) {
                    case 1:
                        valueToReturn = 'One'
                        break
                    case 2:
                        valueToReturn = []
                        valueToReturn << 'Two'
                        break
                }
                valueToReturn
            }
            def v1 = getValueForNumber(1)
            def v2 = getValueForNumber(2)
            def v3 = getValueForNumber(3)
            assert v1 == 'One'
            assert v2 == ['Two']
            assert v3 == null
        '''
    }

    void testNumberPrefixPlusPlusInference() {
        [Byte:'Integer',
         Character: 'Character',
         Short: 'Integer',
         Integer: 'Integer',
         Long: 'Long',
         Float: 'Double',
         Double: 'Double',
         BigDecimal: 'BigDecimal',
         BigInteger: 'BigInteger'
        ].each { orig, dest ->
            assertScript """
            $orig b = 65 as $orig
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def rit = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert rit == make($dest)
            })
            def pp = ++b
            println '++${orig} -> ' + pp.class + ' ' + pp
            assert pp.class == ${dest}
            """
        }
    }

    void testNumberPostfixPlusPlusInference() {
        [Byte:'Byte',
         Character: 'Character',
         Short: 'Short',
         Integer: 'Integer',
         Long: 'Long',
         Float: 'Float',
         Double: 'Double',
         BigDecimal: 'BigDecimal',
         BigInteger: 'BigInteger'
        ].each { orig, dest ->
            assertScript """
            $orig b = 65 as $orig
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def rit = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert rit == make($dest)
            })
            def pp = b++
            println '${orig}++ -> ' + pp.class + ' ' + pp
            assert pp.class == ${dest}
            """
        }
    }

    // GROOVY-6522
    void testInferenceWithImplicitClosureCoercion() {
        assertScript '''
            interface CustomCallable<T> {
                T call()
            }

            class Thing {
                static <T> T customType(CustomCallable<T> callable) {
                    callable.call()
                }

                @ASTTest(phase=INSTRUCTION_SELECTION,value={
                    lookup('test').each {
                        def call = it.expression
                        def irt = call.getNodeMetaData(INFERRED_TYPE)
                        assert irt == LIST_TYPE
                    }
                })
                static void run() {
                    test: customType { [] } // return type is not inferred - fails compile
                }
            }

            Thing.run()
        '''
    }

    void testInferenceWithImplicitClosureCoercionAndArrayReturn() {
        assertScript '''
            interface ArrayFactory<T> { T[] array() }

            public <T> T[] intArray(ArrayFactory<T> f) {
                f.array()
            }
            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE.makeArray()
            })
            def array = intArray { new Integer[8] }
            assert array.length == 8
        '''
    }

    void testInferenceWithImplicitClosureCoercionAndListReturn() {
        assertScript '''
            interface ListFactory<T> { List<T> list() }

            public <T> List<T> list(ListFactory<T> f) {
                f.list()
            }

            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                def irt = node.getNodeMetaData(INFERRED_TYPE)
                assert irt == LIST_TYPE
                assert irt.genericsTypes[0].type == Integer_TYPE
            })
            def res = list { new LinkedList<Integer>() }
            assert res.size() == 0
        '''
    }

    // GROOVY-6835
    void testFlowTypingWithInstanceofAndInterfaceTypes() {
        assertScript '''
            class ShowUnionTypeBug {
                Map<String, Object> instanceMap = (Map<String,Object>)['a': 'Hello World']
                def findInstance(String key) {
                    Set<? extends CharSequence> allInstances = [] as Set
                    def instance = instanceMap.get(key)
                    if(instance instanceof CharSequence) {
                       allInstances.add(instance)
                    }
                    allInstances
                }
            }
            assert new ShowUnionTypeBug().findInstance('a') == ['Hello World'] as Set
        '''
    }

    void testInferenceWithImplicitClosureCoercionAndGenericTypeAsParameter() {
        assertScript '''
            interface Action<T> { void execute(T t) }

            public <T> void exec(T t, Action<T> f) {
                f.execute(t)
            }

            exec('foo') { println it.toUpperCase() }
        '''
    }

    // GROOVY-6574
    void testShouldInferPrimitiveBoolean() {
        assertScript '''
            def foo(Boolean o) {
                @ASTTest(phase=INSTRUCTION_SELECTION,value={
                    assert node.getNodeMetaData(INFERRED_TYPE) == boolean_TYPE
                })
                boolean b = o
                println b
            }
        '''
    }

    // GROOVY-9077
    void testInferredTypeForPropertyThatResolvesToMethod() {
        assertScript '''
            import groovy.transform.*
            import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DIRECT_METHOD_CALL_TARGET

            @CompileStatic
            void meth() {
                def items = [1, 2] as LinkedList

                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    node = node.rightExpression
                    assert node.class.name.contains('PropertyExpression')
                    def target = node.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                    assert target != null
                    assert target.declaringClass.name == 'java.util.LinkedList'
                })
                def one = items.first

                @ASTTest(phase=CLASS_GENERATION, value={
                    node = node.rightExpression
                    assert node.class.name.contains('MethodCallExpression')
                    def target = node.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                    assert target != null
                    assert target.declaringClass.name == 'java.util.LinkedList'
                })
                def alsoOne = items.peek()
            }

            meth()
        '''
    }
}
