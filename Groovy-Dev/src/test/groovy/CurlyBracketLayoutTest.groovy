package groovy

class CurlyBracketLayoutTest extends GroovyTestCase
{
    void testBracketPlacement()
    {
        def foo = "abc"

        if (foo.contains("b"))
        {
            println "Worked a treat. foo = $foo"
        }
        else
        {
            fail("Should have found 'b' inside $foo")
        }

        def list = [1, 2, 3]
        list.each
        {
            println it
        }
    }
}