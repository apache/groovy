/**
 * Tests for method missing handling in Groovy
 
 * @author Graeme Rocher
 * @since 1.1
  *
 * Created: Jul 17, 2007
 * Time: 3:48:15 PM
 * 
 */

package groovy.lang
class MethodMissingTest extends GroovyTestCase {


    void testOverrideStaticMethodMissingTwice() {
        MMTest2.metaClass.'static'.methodMissing = { String name, args -> "foo" }
        assertEquals "foo",MMTest2.doStuff()
        MMTest2.metaClass.'static'.methodMissing = { String name, args -> "bar" }
        assertEquals "bar",MMTest2.doStuff()
    }

    void testSimpleMethodMissing() {
        def t = new MMTest()
        assertEquals "world", t.hello()
        assertEquals "foo", t.stuff()
    }

    void testMethodMissingViaMetaClass() {
        def t1 = new MMTest2()
        shouldFail(MissingMethodException) {
            t1.stuff()
        }
        assertEquals "world", t1.hello()
        MMTest2.metaClass.methodMissing = { String name, args ->
            "foo"
        }
        def t2 = new MMTest2()
        assertEquals "world", t2.hello()
        assertEquals "foo", t2.stuff()
    }

    void testMethodMissingWithRegistration() {
        MMTest2.metaClass.methodMissing = { String name, args ->
             MMTest2.metaClass."$name" = {-> "bar" }        
            "foo"
        }
        def t2 = new MMTest2()
        assertEquals "world", t2.hello()
        assertEquals "foo", t2.stuff()
        assertEquals "bar", t2.stuff()

    }


    void testStaticMethodMissingViaMetaClass() {

        assertEquals "world", MMTest3.hello()
        shouldFail(MissingMethodException) {
            MMTest3.stuff()            
        }
        MMTest3.metaClass.'static'.methodMissing = { String name, args->
             "foo"
        }

        assertEquals "world", MMTest3.hello()
        assertEquals "foo", MMTest3.stuff()
    }

    void testMethodMissingWithInheritance() {
         assertEquals "world",MMTest6.hello()
         assertEquals "cruel world",MMTest6.goodbye()

         MMTest6.metaClass.'static'.methodMissing = { String name, args ->
            "foo"
         }
         assertEquals "foo",MMTest6.bar()

         shouldFail(MissingMethodException) {
             MMTest5.bar()
         }
    }

}
class MMTest {
    def hello() { "world" }
    def methodMissing(String name, args) {
        "foo"
    }
}
class MMTest2 {
    def hello() { "world" }
}
class MMTest3 {
    static hello() { "world" }
}

class MMTest5 {
    static hello() { "world" }
}
class MMTest6 extends MMTest5 {
    static goodbye() { "cruel world" }
}