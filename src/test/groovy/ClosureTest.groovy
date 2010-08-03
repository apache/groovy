package groovy

/** 
 * Tests Closures in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureTest extends GroovyTestCase {

    def count

    void testSimpleBlockCall() {
        count = 0

        def block = {owner-> owner.incrementCallCount() }

        assertClosure(block)
        assert count == 1

        assertClosure({owner-> owner.incrementCallCount() })
        assert count == 2
    }

    void testVariableLengthParameterList() {

        def c1 = {Object[] args -> args.each{count += it}}

        count = 0
        c1(1, 2, 3)
        assert count == 6

        count = 0
        c1(1)
        assert count == 1

        count = 0
        c1([1, 2, 3] as Object[])
        assert count == 6

        def c2 = {a, Object[] args -> count += a; args.each{count += it}}

        count = 0
        c2(1, 2, 3)
        assert count == 6

        count = 0
        c2(1)
        assert count == 1

        count = 0
        c2(1, [2, 3] as Object[])
        assert count == 6
    }

    void testBlockAsParameter() {
        count = 0

        callBlock(5, {owner-> owner.incrementCallCount() })
        assert count == 6

        callBlock2(5, {owner-> owner.incrementCallCount() })
        assert count == 12
    }
  
    void testMethodClosure() {
        def block = this.&incrementCallCount

        count = 0

        block.call()

        assert count == 1

        block = System.out.&println

        block.call("I just invoked a closure!")
    }
  
    def incrementCallCount() {
        //System.out.println("invoked increment method!")
        count = count + 1
    }

    def assertClosure(Closure block) {
        assert block != null
        block.call(this)
    }

    protected void callBlock(Integer num, Closure block) {
        for ( i in 0..num ) {
            block.call(this)
        }
    }

    protected void callBlock2(num, block) {
        for ( i in 0..num ) {
            block.call(this)
        }
    }


    int numAgents = 4
    boolean testDone = false

    void testIntFieldAccess() {
        def agents = new ArrayList();
        numAgents.times {
            TinyAgent btn = new TinyAgent()
            testDone = true
            btn.x = numAgents
            agents.add(btn)
        }
        assert agents.size() == numAgents
    }

    void testWithIndex() {
        def str = ''
        def sum = 0
        ['a','b','c','d'].eachWithIndex { item, index -> str += item; sum += index }
        assert str == 'abcd' && sum == 6
    }

    void testMapWithEntryIndex() {
        def keyStr = ''
        def valStr = ''
        def sum = 0
        ['a':'z','b':'y','c':'x','d':'w'].eachWithIndex { entry, index ->
            keyStr += entry.key
            valStr += entry.value
            sum += index
        }
        assert keyStr == 'abcd' && valStr == 'zyxw' && sum == 6
    }

    void testMapWithKeyValueIndex() {
        def keyStr = ''
        def valStr = ''
        def sum = 0
        ['a':'z','b':'y','c':'x','d':'w'].eachWithIndex { k, v, index ->
            keyStr += k
            valStr += v
            sum += index
        }
        assert keyStr == 'abcd' && valStr == 'zyxw' && sum == 6
    }

    /**
    * Test access to Closure's properties
    * cf GROOVY-2089
    */
    void testGetProperties() {
        def c = { println it }

        assert c.delegate == c.getDelegate()
        assert c.owner == c.getOwner()
        assert c.maximumNumberOfParameters == c.getMaximumNumberOfParameters()
        assert c.parameterTypes == c.getParameterTypes()
        assert c.class == c.getClass()
        assert c.directive == c.getDirective()
        assert c.resolveStrategy == c.getResolveStrategy()
        assert c.thisObject == c.getThisObject()

        // no idea why this one fails
        // assert c.metaClass == c.getMetaClass()
    }

    void testGetPropertiesGenerically() {
        Closure.metaClass.properties.each { property ->
            def closure = { println it }
            closure."$property.name" == closure."${MetaProperty.getGetterName(property.name, property.type)}"()
        }
    }

    void testSetProperties() {
        def c = { println it }

        def myDelegate = new Object()
        c.delegate = myDelegate
        assert c.getDelegate() == myDelegate

        c.resolveStrategy = Closure.DELEGATE_ONLY
        assert c.getResolveStrategy() == Closure.DELEGATE_ONLY

        c.directive = Closure.DONE
        assert c.directive == Closure.DONE

        // like in testGetProperties(), don't know how to test metaClass property
    }
    
    /**
     * GROOVY-2150 ensure list call is available on closure
     */
    void testCallClosureWithlist() {
      def list = [1,2]
      def cl = {a,b->a+b }
      assert cl(list)==3
    }
}

public class TinyAgent {
    int x
}

