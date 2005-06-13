/**
 * Expose how to deal with multi-dimensional Arrays until this is supported at the language level.
 * @author Dierk Koenig
 */
 
package groovy;

public class MultiDimArraysTest extends GroovyTestCase {

    void notSupported_testCallTwoDimStringArray(){
        def someStringArray =  new SomeClass().anArrayOfStringArrays()
        assert 1 == someStringArray.size()
    }
    
    void testCallTwoDimStringArrayWorkaround(){
        def someStringArray =  new SomeClass().anArrayOfStringArraysWorkaround()
        assert 1 == someStringArray.size()
        assert "whatever" == someStringArray[0][0]
    }
}