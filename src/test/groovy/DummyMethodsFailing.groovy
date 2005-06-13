/**
 * @author Dierk Koenig
 */
 
package groovy;

public class DummyMethodsFailing extends GroovyTestCase {
    void testFailing(){
        def someStringArray =  new SomeClass().anArrayOfStringArrays()
        assert 1 == someStringArray.size()
    }
}