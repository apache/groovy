/**
 * Tests the respondsTo functionality of Groovy
 
 * @author Graeme Rocher
 * @since 1.1
  *
 * Created: Sep 7, 2007
 * Time: 4:35:19 PM
 * 
 */
package groovy.lang
class RespondsToTest extends GroovyTestCase {

    void testRespondsTo() {

        RTTest2.metaClass.newM = {-> "foo" }

        def t = new RTTest2()

        assert t.metaClass.respondsTo(t,"one")
        assert t.metaClass.respondsTo(t,"three")
        assert t.metaClass.respondsTo(t,"one", String)
        assert t.metaClass.respondsTo(t,"foo", String)
        assert t.metaClass.respondsTo(t,"bar", String)
        assert t.metaClass.respondsTo(t,"stuff")
        //assert t.metaClass.respondsTo(t,"two") THIS DOESN'T WORK! Should responds to deal with closure properties?
        assert t.metaClass.respondsTo(t,"getFive")
        assert t.metaClass.respondsTo(t,"setFive")
        assert t.metaClass.respondsTo(t,"setFive", String)
        assert t.metaClass.respondsTo(t,"newM")
        assert !t.metaClass.respondsTo(t,"one", String, Integer)
    }

    void testHasProperty() {
        RTTest2.metaClass.getNewProp = {-> "new" }
        def t = new RTTest2()

        assert t.metaClass.hasProperty(t,"two")
        assert t.metaClass.hasProperty(t,"five")
        assert t.metaClass.hasProperty(t,"six")
        assert t.metaClass.hasProperty(t,"seven")
        assert t.metaClass.hasProperty(t,"eight")
        assert t.metaClass.hasProperty(t,"newProp")

    }
}
class RTTest1 {
    String five
    def two = { "three" }
    def one() { "two"}
    def one(String one) { "two: $one" }
    def three(String one) { "four" }
    def three(Integer one) { "four" }
    def foo(String name) {
        "bar"
    }

    String getSeven() { "seven" }
}
class RTTest2 extends RTTest1 {
    String six
    def bar(String name) { "foo" }
    static stuff() { "goodie" }

    String getEight() { "eight" }
}