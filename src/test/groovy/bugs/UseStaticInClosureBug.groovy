/**
 * @version $Revision$
 */
class UseStaticInClosureBug extends GroovyTestCase {

    static stuff = [:]

    void testBug() {
        [1,2,3].each { stuff[it] = "dog" }

        assert stuff.size() == 3
        assert stuff[2] == "dog"
    }

    void testBug2() {
        doStatic()
    }

    static doStatic() {
        [1,2,3].each { stuff[it] = "dog" }

        assert stuff.size() == 3
        assert stuff[2] == "dog"
    }
}
