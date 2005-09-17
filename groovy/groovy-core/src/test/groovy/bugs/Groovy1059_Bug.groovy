package groovy.bugs

/**
 * TODO: GROOVY-1059
 *
 *    Accessible to a closure attribute of an abject with the operator ".@".
 *    For examples, all of the expressions
 *
 *            object.@closure()
 *            object.@closure.call()
 *            object.@closure.doCall()
 *            (object.@closure)()
 *
 *    have the same meaning.
 *
 * @author  John Wilson
 * @author  Pilho Kim
 */

class Groovy1059_Bug extends GroovyTestCase {

    void testClosureAsAttribute() {
        def x = new Groovy1059Foo()

        println( x.say() )
        println( (x.@say)() )
        println( x.@say() )  // TODO: Groovy-1059 should work
        println( x.@say.call() )
        println( x.@say.doCall() )
        println( x.@say2() )

        assert "I am a Method" == x.say()
        assert "I am a Method" == x.@say2()
        assert "I am a Closure" == (x.@say)()
        assert "I am a Closure" == x.@say()
        assert x.@say() == (x.@say)()
        assert x.@say() == x.@say.call()
        assert x.@say() == x.@say.doCall()
        assert x.@say() != x.say()
        assert x.@say2() == x.say()
    }

}

class Groovy1059Foo {

    def public say = { it -> return "I am a Closure" }
    def public say2 = this.&say

    public Object say() {
       return "I am a Method"
    }
}
