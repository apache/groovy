/*
 * Copyright 2004-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.lang

/**
 * @author Graeme Rocher
 */

class ExpandoMetaClassTest extends GroovyTestCase {

    void testMethodsAfterAddingNewMethod() {
        Test.metaClass.newMethod ={-> "foo" }

        def methods = Test.metaClass.methods.findAll {it.name == "newMethod"}
        assert methods
        assertEquals 1, methods.size()

        Test.metaClass.newMethod ={-> "foo" }

        methods = Test.metaClass.methods.findAll {it.name == "newMethod"}
        assert methods
        assertEquals 1, methods.size()

    }

    void testPropertiesAfterAddingProperty() {
        Test.metaClass.getNewProp ={-> "foo" }

        def props = Test.metaClass.properties.findAll {it.name == "newProp"}
        assert props
        assertEquals 1, props.size()

        Test.metaClass.setNewProp ={String txt-> }

        props = Test.metaClass.properties.findAll {it.name == "newProp"}
        assert props
        assertEquals 1, props.size()

    }

    void testOverrideStaticMethod() {        
        TestStatic.metaClass.'static'.f = { "first" }
        assertEquals "first",TestStatic.f("")

        TestStatic.metaClass.'static'.f = { "second" }
        assertEquals "second",TestStatic.f("")
    }


    void testOverrideMethod() {
        TestStatic.metaClass.f = { "first" }
        assertEquals "first",new TestStatic ().f("")

        TestStatic.metaClass.f = { "second" }
        assertEquals "second",new TestStatic ().f("")
    }


    void testStaticBeanStyleProperties() {
        def mc = new ExpandoMetaClass(TestInvokeMethod.class, true, true)
        mc.initialize()
        GroovySystem.metaClassRegistry.setMetaClass(TestInvokeMethod.class, mc)

        mc.'static'.getHello = {-> "bar!"}

        assertEquals "bar!", TestInvokeMethod.hello
    }

    void testOverrideInvokeStaticMethod() {
        def mc = new ExpandoMetaClass(TestInvokeMethod.class, true, true)
        mc.initialize()
        GroovySystem.metaClassRegistry.setMetaClass(TestInvokeMethod.class, mc)

        mc.'static'.invokeMethod = {String methodName, args ->
            def metaMethod = mc.getStaticMetaMethod(methodName, args)
            def result = null
            if (metaMethod) result = metaMethod.invoke(delegate, args)
            else {
                result = "foo!"
            }
            result
        }

        assertEquals "bar!", TestInvokeMethod.myStaticMethod()
        assertEquals "foo!", TestInvokeMethod.dynamicMethod()
    }

    void testOverrideInvokeMethod() {
        def mc = new ExpandoMetaClass(TestInvokeMethod.class, false, true)
        mc.initialize()


        assert mc.hasMetaMethod("invokeMe", [String] as Class[])

        mc.invokeMethod = {String name, args ->
            def mm = delegate.metaClass.getMetaMethod(name, args)
            mm ? mm.invoke(delegate, args) : "bar!!"
        }

        def t = new TestInvokeMethod()
        t.metaClass = mc

        assertEquals "bar!!", t.doStuff()
        assertEquals "Foo!! hello", t.invokeMe("hello")
    }

    void testOverrideSetProperty() {
        def mc = new ExpandoMetaClass(TestGetProperty.class, false, true)
        mc.initialize()

        assert mc.hasMetaProperty("name")

        def testValue = null
        mc.setProperty = {String name, value ->
            def mp = delegate.metaClass.getMetaProperty(name)

            if (mp) {mp.setProperty(delegate, value)} else {testValue = value}
        }

        def t = new TestGetProperty()
        t.metaClass = mc

        t.name = "Bob"
        assertEquals "Bob", t.name

        t.foo = "bar"
        assertEquals "bar", testValue
    }

    void testOverrideGetProperty() {
        def mc = new ExpandoMetaClass(TestGetProperty.class, false , true)
        mc.initialize()

        assert mc.hasMetaProperty("name")

        mc.getProperty = {String name ->
            def mp = delegate.metaClass.getMetaProperty(name)

            mp ? mp.getProperty(delegate) : "foo $name"
        }

        def t = new TestGetProperty()
        t.metaClass = mc

        assertEquals "foo bar", t.getProperty("bar")
        assertEquals "foo bar", t.bar
        assertEquals "Fred", t.getProperty("name")
        assertEquals "Fred", t.name
    }

    void testBooleanGetterWithClosure() {
        def metaClass = new ExpandoMetaClass(Test.class, false, true)
        metaClass.initialize()
        metaClass.isValid = {-> true}

        def t = new Test()
        t.metaClass = metaClass

        assert t.isValid()
        assert t.valid
    }


    void testAllowAdditionOfProperties() {
        def metaClass = new ExpandoMetaClass(Test.class, false, true)

        metaClass.getOne << {->
            "testme"
        }
        metaClass.initialize()
        try {
            metaClass.getTwo << {->
                "testagain"
            }
        }
        catch (RuntimeException e) {
            fail("Should have allowed addition of new method")
        }

        def t = new Test()
        t.metaClass = metaClass

        assertEquals "testme", t.one
        assertEquals "testagain", t.two
    }

    void testAllowAdditionOfMethods() {
        def metaClass = new ExpandoMetaClass(Test.class, false, true)

        metaClass.myMethod << {->
            "testme"
        }
        metaClass.initialize()
        try {
            metaClass.mySecondMethod << {->
                "testagain"
            }
        }
        catch (RuntimeException e) {
            fail("Should have allowed addition of new method")
        }

        def t = new Test()
        t.metaClass = metaClass

        assertEquals "testme", t.myMethod()
        assertEquals "testagain", t.mySecondMethod()
    }

    void testForbiddenAdditionOfMethods() {
        def metaClass = new ExpandoMetaClass(Test.class)

        metaClass.myMethod << {
            "testme"
        }
        metaClass.initialize()

        def t = new Test()

        try {
            metaClass.mySecondMethod << {
                "testagain"
            }
            fail("Should have thrown exception")
        }
        catch (RuntimeException e) {
            // expected
        }
    }

    void testPropertyGetterWithClosure() {
        def metaClass = new ExpandoMetaClass(Test.class)

        metaClass.getSomething = {-> "testme"}

        metaClass.initialize()

        def t = new Test()
        t.metaClass = metaClass

        assertEquals "testme", t.getSomething()
        assertEquals "testme", t.something
    }

    void testPropertySetterWithClosure() {
        def metaClass = new ExpandoMetaClass(Test.class)

        def testSet = null
        metaClass.setSomething = {String txt -> testSet = txt}

        metaClass.initialize()

        def t = new Test()
        t.metaClass = metaClass

        t.something = "testme"
        assertEquals "testme", testSet

        t.setSomething("test2")
        assertEquals "test2", testSet
    }

    void testNewMethodOverloading() {
        def metaClass = new ExpandoMetaClass(Test.class)

        metaClass.overloadMe << {String txt -> txt} << {Integer i -> i}

        metaClass.initialize()

        def t = new Test()
        t.metaClass = metaClass

        assertEquals "test", t.overloadMe("test")
        assertEquals 10, t.overloadMe(10)
    }

    void testOverloadExistingMethodAfterInitialize() {
        def t = new Test()

        assertEquals "test", t.doSomething("test")

        def metaClass = new ExpandoMetaClass(Test.class, false, true)
        metaClass.initialize()

        metaClass.doSomething = {Integer i -> i + 1}

        t.metaClass = metaClass

        assertEquals "test", t.doSomething("test")
        assertEquals 11, t.doSomething(10)
    }

    void testOverloadExistingMethodBeforeInitialize() {
        def t = new Test()

        assertEquals "test", t.doSomething("test")

        def metaClass = new ExpandoMetaClass(Test.class, false, true)


        metaClass.doSomething = {Integer i -> i + 1}

        metaClass.initialize()

        t.metaClass = metaClass

        assertEquals "test", t.doSomething("test")
        assertEquals 11, t.doSomething(10)
    }

    void testNewPropertyMethod() {
        def metaClass = new ExpandoMetaClass(Test.class)

        metaClass.something = "testme"

        metaClass.initialize()

        def t = new Test()
        t.metaClass = metaClass

        assertEquals "testme", t.getSomething()
        assertEquals "testme", t.something

        t.something = "test2"
        assertEquals "test2", t.something
        assertEquals "test2", t.getSomething()

        def t2 = new Test()
        t2.metaClass = metaClass
        // now check that they're not sharing the same property!
        assertEquals "testme", t2.something
        assertEquals "test2", t.something

        t2.setSomething("test3")

        assertEquals "test3", t2.something
    }

    void testCheckFailOnExisting() {
        def metaClass = new ExpandoMetaClass(Test.class)
        try {
            metaClass.existing << {->
                "should fail. already exists!"
            }
            fail("Should have thrown exception when method already exists")
        }
        catch (Exception e) {
            // expected
        }
    }

    void testCheckFailOnExistingConstructor() {
        def metaClass = new ExpandoMetaClass(Test.class)
        try {
            metaClass.constructor << {->
                "should fail. already exists!"
            }
            fail("Should have thrown exception when method already exists")
        }
        catch (Exception e) {
            // expected
        }
    }

    void testCheckFailOnExistingStaticMethod() {
        def metaClass = new ExpandoMetaClass(Test.class)
        try {
            metaClass.'static'.existingStatic << {->
                "should fail. already exists!"
            }
            fail("Should have thrown exception when method already exists")
        }
        catch (Exception e) {
            // expected
        }
    }

    void testNewStaticMethod() {
        def metaClass = new ExpandoMetaClass(Test.class, true)

        metaClass.'static'.myStaticMethod << {String txt ->
            "testme"
        }
        metaClass.initialize()

        assertEquals "testme", Test.myStaticMethod("blah")
    }

    void testReplaceStaticMethod() {
        def metaClass = new ExpandoMetaClass(Test.class, true)

        metaClass.'static'.existingStatic = {->
            "testme"
        }
        metaClass.initialize()

        assertEquals "testme", Test.existingStatic()

    }

    void testNewZeroArgumentStaticMethod() {
        def metaClass = new ExpandoMetaClass(Test.class, true)

        metaClass.'static'.myStaticMethod = {->
            "testme"
        }
        metaClass.initialize()

        assertEquals "testme", Test.myStaticMethod()
    }

    void testNewInstanceMethod() {
        def metaClass = new ExpandoMetaClass(Test.class)

        metaClass.myMethod << {
            "testme"
        }
        metaClass.initialize()

        def t = new Test()

        t.metaClass = metaClass

        assertEquals "testme", t.myMethod()
    }

    void testNewConstructor() {
        def metaClass = new ExpandoMetaClass(Test.class, true)

        metaClass.constructor << {String txt ->
            def t = Test.class.newInstance()
            t.name = txt
            return t
        }

        metaClass.initialize()

        def t = new Test("testme")
        assert t
        assertEquals "testme", t.name

        GroovySystem.metaClassRegistry.removeMetaClass(Test.class)
    }

    void testReplaceConstructor() {
        def metaClass = new ExpandoMetaClass(Test.class, true)

        metaClass.constructor = {->
            "testme"
        }

        metaClass.initialize()

        def t = new Test()
        assert t
        assertEquals "testme", t

        GroovySystem.metaClassRegistry.removeMetaClass(Test.class)
    }

    void testReplaceInstanceMethod() {
        def metaClass = new ExpandoMetaClass(Test.class)

        metaClass.existing2 = {Object i ->
            "testme"
        }
        metaClass.initialize()

        def t = new Test()
        t.metaClass = metaClass

        def var = 1
        assertEquals "testme", t.existing2(var)
    }

    void testBorrowMethodFromAnotherClass() {
        def metaClass = new ExpandoMetaClass(Test.class)

        def a = new Another()
        metaClass.borrowMe = a.&another
        metaClass.borrowMeToo = a.&noArgs
        metaClass.initialize()

        def t = new Test()
        t.metaClass = metaClass

        assertEquals "mine blah!", t.borrowMe("blah")
        assertEquals "mine blah+foo!", t.borrowMe("blah", "foo")
        // GROOVY-1993
        assertEquals "no args here!", t.borrowMeToo()
    }

    void testClosureWithOptionalArgs() {
        def metaClass = new ExpandoMetaClass(Test.class)

        metaClass.withOptional {
            String first, String second = "no param"  ->
            "$first + $second"
        }

        def t = new Test()
        t.metaClass = metaClass

        assertEquals ("blah + no param", t.withOptional("blah"))
        assertEquals ("blah + foo", t.withOptional("blah", "foo"))
    }

    void testBorrowByName() {
        println 'testBorrowByName'
        def metaClass = new ExpandoMetaClass(Test.class)

        def a = new Another()
        metaClass.borrowMe = a.&'another'
        metaClass.borrowMeToo = a.&'noArgs'
        metaClass.initialize()

        def t = new Test()
        t.metaClass = metaClass                                                

        assertEquals "mine blah!", t.borrowMe("blah")
        // GROOVY-1993
        assertEquals "no args here!", t.borrowMeToo()
    }

    void testAddIdenticalPropertyToChildAndParent() {
        ExpandoMetaClass.enableGlobally()
        doMethods(SuperClass.class)
        doMethods(ChildClass.class)

        def child = new ChildClass()
        def parent = new SuperClass()

        assert parent.errors == null
        parent.errors = [3, 2, 1, 0]
        assert parent.errors.size() == 4

        assert child.errors == null
        child.errors = [1, 2, 3]
        assert child.errors.size() == 3
        //TODO disable ExpandoMetaClass
        ExpandoMetaClass.disableGlobally()
    }

    void testMissingPropertyClosure() {
        assertScript """
            class Circle {
                String prop1 = 'value1'
            }

            ExpandoMetaClass emc = new ExpandoMetaClass(this.class, false)
            emc.methodMissing = {String name, args ->
                throw new MissingMethodException(name, Script, args)
            }
            emc.propertyMissing = {String name ->
                throw new MissingPropertyException(name, Script)
            }
            emc.initialize()
            this.metaClass = emc

            Circle circle = new Circle()
            // closure will try to access prop1 on script, which does not have such
            // a property, thus a MissingPropertyException will be thrown
            // causing the closure code to select the delegate to resolve the property
            Closure cl = {prop1}
            cl.delegate = circle
            assert cl.call()=='value1'
        """
    }

    void testMissingMethodClosure() {
        assertScript """
            class Circle {
                def m1(){1}
            }

            ExpandoMetaClass emc = new ExpandoMetaClass(this.class, false)
            emc.methodMissing = {String name, args ->
                throw new MissingMethodException(name, Script, args)
            }
            emc.propertyMissing = {String name ->
                throw new MissingPropertyException(name, Script)
            }
            emc.initialize()
           this.metaClass = emc

            Circle circle = new Circle()
            // closure will try to call m1 on script, which does not have such
            // a method, thus a MissingMethodException will be thrown
            // causing the closure code to select the delegate to call the method
            Closure cl = {m1()}
            cl.delegate = circle
            assert cl.call() == 1
        """
    }

    void testMissingMethodExceptionThrownFromMissingMethod() {
        assertScript """
           class Circle {
                def invokeMethodInvocations = 0
                def invokeMethod(String name, Object[] args) {
                    invokeMethodInvocations++;
                }
                def callNonExistingMethod() {
                  m1()
                }
            }

            ExpandoMetaClass emc = new ExpandoMetaClass(Circle.class, false)
            def exception = new MissingMethodException("m1", Circle, null)
            emc.methodMissing = {String name, args ->
                throw exception
            }
            emc.initialize()

            Circle circle = new Circle()
            circle.metaClass = emc

            assert circle.invokeMethodInvocations == 0
            def gotException=true
            try {
               circle.callNonExistingMethod()
               gotException=false
            } catch (MissingMethodException mme) {
               gotException=true
               assert mme == exception
            }
            assert gotException,"MissingMethodException expected, but got something else"

            assert circle.invokeMethodInvocations == 0
            gotException=true
            try {
               circle.m1()
               gotException=false
            } catch (MissingMethodException mme) {
               assert mme == exception
            }
            assert gotException,"MissingMethodException expected, but got something else"
        """
    }

    def doMethods(clazz) {
        def metaClass = clazz.metaClass

        metaClass.setErrors = {errors ->
            thingo = errors
        }

        metaClass.getErrors = {->
            return thingo
        }
    }

    void testGetProperty () {
        def x = new SuperClass ()
        def mc = x.metaClass
        mc.getProperty = { String name ->
                MetaProperty mp = mc.getMetaProperty(name)
                if (mp)
                  mp.getProperty(delegate)
                else {
                  if (thingo) {
                      thingo."$name"
                  }
                  else {
                      if (application) {
                          "non-null application"
                      }
                      else {
                          String methodName = "get${name[0].toUpperCase()}${name.substring(1)}"
                          mc."$methodName" = {
                              -> "$name"
                          }
                          delegate."$methodName" ()
                      }
                  }
                }
            }

        Map map = ["prop": "none"]
        x.thingo = map
        assertEquals map, x.thingo
        assertEquals ("none", x.prop )
        x.thingo = null
        application = new Object ()
        assertEquals ("non-null application", x.prop)
        application = null
        assertEquals ("prop", x.prop)
        application = new Object ()
        assertEquals ("prop", x.prop)
    }

    def application

    void testWithNull () {
        SuperClass.metaClass = null
        ExpandoMetaClass.enableGlobally()
        def x = new SuperClass ()
        def mc = x.metaClass

        def request = new Object ()
        request.metaClass {
            getFormat {
                -> "js"
            }
        }

        mc.render = {String txt ->
            txt
        }

        assertEquals ("js",x.render(request.format))
        ExpandoMetaClass.disableGlobally()
    }

    void testInterfaceWithGetProperty () {
        Implemented.metaClass.getProperty = { String name ->
            return "META " + delegate.class.metaClass.getMetaProperty(name).getProperty(delegate)
        }
        InterfaceWithFormat.metaClass.getFormat = {
            -> "js"
        }
        def x = new Implemented()
        assertEquals "META js", x.format
    }
    
    void testHasMethodForVarg() {
       // as of 1.6 a metaClass is often the HandleMetaclass, which delegates
       // methods to an underlaying meta class. hasMethod is a Method on EMC
       // that takes a Class[] vargs argument as last part. If that part is not
       // given, then hasMetaMethod will still work, but the code actually 
       // invoking the method in EMC.invokeMehod(String,Object) has to correct the
       // arguments. 
       def mc = "".metaClass
       assert mc.class == org.codehaus.groovy.runtime.HandleMetaClass
       "".metaClass.hasMetaMethod("trim")
    }
}

interface InterfaceWithFormat {

}

class Implemented implements InterfaceWithFormat {

}

class SuperClass {
    def thingo
}

class ChildClass extends SuperClass {

}

class TestInvokeMethod {
    def invokeMe(String boo) {"Foo!! $boo"}

    static myStaticMethod() {"bar!"}
}

class TestGetProperty {
    String name = "Fred"
}

class Test {
    String name

    def existing2(obj) {
        "hello2!"
    }
    def existing() {
        "hello!"
    }

    def doSomething(Object txt) {txt}

    static existingStatic() {
        "I exist"
    }
}

class Another {
    def another(txt, additional="") {
        "mine ${txt}${additional?'+'+additional:''}!"
    }
    def noArgs() {
        "no args here!"
    }
}

class Child extends Test {
    def aChildMethod() {
        "hello children"
    }
}
class TestStatic {}