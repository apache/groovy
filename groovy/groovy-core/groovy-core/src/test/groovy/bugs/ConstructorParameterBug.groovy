package groovy.bugs

class ConstructorParameterBug extends GroovyTestCase {

    void testMethodWithNativeArray() {
        int[] value = [2*2]
        println "${value} of type ${value.class}"
        /** @todo fixme!
    	blah2(value)
    	*/
    }

    def blah2(int[] wobble) {
       println(wobble)
    }

}
