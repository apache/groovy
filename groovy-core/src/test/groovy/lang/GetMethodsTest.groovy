/**
 * Tests the behaviour of the runtime evaluating methods of Groovy's MetaClass system
 
 * @author Graeme Rocher
 * @since 1.1
  *
 * Created: Sep 10, 2007
 * Time: 8:17:29 AM
 * 
 */
package groovy.lang
class GetMethodsTest extends GroovyTestCase {


    void testGetMethods() {
       GMTest2.metaClass.doStuff = {-> "foo" }
       GMTest2.metaClass.getFoo = {-> "foo" }
       GMTest2.metaClass.bar = "bar"
       GMTest2.metaClass.'static'.doMoreStuff = {-> "more" }
       def t = new GMTest2()


       assert t.metaClass.methods.find { it.name == 'one' }
       assert t.metaClass.methods.find { it.name == 'three' }
       assert t.metaClass.methods.find { it.name == 'foo' }
       assert t.metaClass.methods.find { it.name == 'getSeven' }
       assert t.metaClass.methods.find { it.name == 'getEight' }
       assert t.metaClass.methods.find { it.name == 'bar' }
       assert t.metaClass.methods.find { it.name == 'toString' }
       assert t.metaClass.methods.find { it.name == 'getEight' }
       assert t.metaClass.methods.find { it.name == 'stuff' }
       assert t.metaClass.methods.find { it.name == 'doStuff' }
       assert t.metaClass.methods.find { it.name == 'getFoo' }
       assert t.metaClass.methods.find { it.name == 'getBar' }
       assert t.metaClass.methods.find { it.name == 'setBar' }
       assert t.metaClass.methods.find { it.name == 'doMoreStuff' }
    }

    void testGetProperties() {

        GMTest2.metaClass.getFoo = {-> "foo" }
        GMTest2.metaClass.bar = "bar"

        def t = new GMTest2()

        assert t.metaClass.properties.find { it.name == 'five' }
        assert t.metaClass.properties.find { it.name == 'six' }
        assert t.metaClass.properties.find { it.name == 'seven' }
        assert t.metaClass.properties.find { it.name == 'eight' }
        assert t.metaClass.properties.find { it.name == 'foo' }
        assert t.metaClass.properties.find { it.name == 'bar' }
    }
}
class GMTest1 {
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
class GMTest2 extends GMTest1 {
    String six
    def bar(String name) { "foo" }
    static stuff() { "goodie" }

    String getEight() { "eight" }
}