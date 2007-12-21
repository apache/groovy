/**
 * Expose how to deal with multi-dimensional Arrays until this is supported at the language level.
 * @author Dierk Koenig
 * @author Jochen Theodorou
 */
 
package groovy;

public class MultiDimArraysTest extends GroovyTestCase {

    // todo: enable as soon as multi dims are supported
    void testCallTwoDimStringArray(){
        def someArrayOfStringArrays =  new SomeClass().anArrayOfStringArrays()
        assert 1 == someArrayOfStringArrays.size()
    }
    
    void testCallTwoDimStringArrayWorkaround(){
        def someArrayOfStringArrays =  new SomeClass().anArrayOfStringArraysWorkaround()
        assert 1 == someArrayOfStringArrays.size()
        assert "whatever" == someArrayOfStringArrays[0][0]
        for (i in 0..<someArrayOfStringArrays.size()) {
            assert someArrayOfStringArrays[i]
        }
    }

    void testCallTwoDimStringArrayWorkaroundWithNull(){
        def someArrayOfStringArrays =  new SomeClass().anArrayOfStringArraysWorkaround()
        assert 1 == someArrayOfStringArrays.size()
        assert "whatever" == someArrayOfStringArrays[0][0]
        someArrayOfStringArrays.each(){ assert it}
    }

    void testInsideGroovyMultiDimReplacement(){
        Object[] someArrayOfStringArrays = [["a","a","a"],["b","b","b",null]]
        assert "a" == someArrayOfStringArrays[0][0]
        someArrayOfStringArrays.each(){ assert it}
    }
    
    void testMultiDimCreationWithSizes(){
        Object[][] objectArray = new Object[2][5]
        assert objectArray.length == 2
        objectArray.each { 
          assert it.length == 5 
          it.each { assert it == null }
        }
    }
    
    void testMultiDimCreationWithoutSizeAtEnd() {
       def array = new int[5][6][]
       assert array.class.name == "[[[I"
       assert array[0].class.name == "[[I"
       assert array[0][0] == null
    }
    
    void testMultiDimArrayForCustomClass() {
		def ff = new MultiDimArraysTest[3][4]
		assert "[[Lgroovy.MultiDimArraysTest;" == ff.class.name;
    }

}

