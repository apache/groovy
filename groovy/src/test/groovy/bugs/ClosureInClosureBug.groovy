package groovy.bugs

/**
 * Bug illustrating the nested closures variable scope visibility issue.
 * l.each is ClosureInClosureBug$1 and it.each is ClosureInClosureBug$2
 * The variable text is not visible from ClosureInClosureBug$2.
 * Indeed, a closure can only see the variable defined outside this closure (one level up)
 * but cannot see what's in the second level.
 *
 * In order to make the test work, do not forget to uncomment the line "println(text)"
 *
 * @authour Guillaume Laforge
 */
class ClosureInClosureBug extends GroovyTestCase {

    void testInvisibleVariable() {
        def text = "test "

        def l = [1..11, 2..12, 3..13, 4..14]

        l.each {
            //println(text)
            it.each{
                println(text)
            }
        }
    }

    static void main(args) {
        def bug = new ClosureInClosureBug()
        bug.testInvisibleVariable()
    }
}