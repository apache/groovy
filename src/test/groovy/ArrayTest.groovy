class ArrayTest extends GroovyTestCase {

    void testFixedSize() {
        array = new String[10]
        
        assert array.size() == 10
        
        array[0] = "Hello"
        
        assert array[0] == "Hello"
        
        println "Created array ${array.inspect()} with type ${array.class}"
    }
    
    void testArrayWithInitializer() {
        String[] array = [ "nice", "cheese", "gromit" ]
        
        println "Created array ${array.inspect()} with type ${array.class}"
        
        assert array.size() == 3
        assert array[0] == "nice" , array.inspect()
        assert array[1] == "cheese"
        assert array[2] == "gromit"
    }

    void testCharArrayCreate() {
           array  = new char[3]
           assert array.size() == 3
    }

    void testCharArrayWithInitializer() {
        char[] array = [ 'a', 'b', 'c' ]
        
        println "Created array ${array.inspect()} with type ${array.class}"
        
        assert array.size() == 3
        assert array[0] == 'a' , array.inspect()
        assert array[1] == 'b'
        assert array[2] == 'c'
    }
    
    void testByteArrayCreate() {
        array = new byte[100]
        assert array.size() == 100;
    }

    void testByteArrayWithInitializer() {
        byte[] array = [0, 1, 2, 3]
        
        println "Created array ${array.inspect()} with type ${array.class}"
        
        assert array.size() == 4
        assert array[0] == 0 , array.inspect()
        assert array[1] == 1
        assert array[2] == 2
        assert array[3] == 3
    }

    void testByteArrayWithInitializerAndAssignmentOfNumber() {
        byte[] array = [ 2, 4]
        println "Created array ${array.inspect()} with type ${array.class}"
        assert array.size() == 2
        assert array[0] == 2
        assert array[1] == 4

        array[0] = 76
        array[1] = 32
        assert array[0] == 76
        assert array[1] == 32

        array.putAt(0, 45)
        array.putAt(1, 67)
        assert array[0] == 45
        assert array[1] == 67
    }

    void testDoubleArrayCreate() {
         array  = new double[3]
         assert array.size() == 3
    }

    void testDoubleArrayWithInitializer() {
        double[] array = [ 1.3, 3.14, 2.7]
        
        println "Created array ${array.inspect()} with type ${array.class}"
        
        assert array.size() == 3
        assert array[0] == 1.3 , array.inspect()
        assert array[1] == 3.14
        assert array[2] == 2.7
    }


    void testIntArrayCreate() {
        array = new int[5]
        
        assert array.size() == 5
    }

    void testIntArrayWithInitializer() {
        int[] array = [42, -5, 360]
        
        println "Created array ${array.inspect()} with type ${array.class}"
        
        assert array.size() == 3
        assert array[0] == 42 , array.inspect()
        assert array[1] == -5
        assert array[2] == 360
    }



    void testArrayDeclaration() {
        String[] array = [ "a", "b", "c" ]

        array.each { element :: println( element ) }

        assert array.size() == 3

    }

    static void main( String[] args ) {
        o = new ArrayTest();

        o.testArrayDeclaration();
    }
}
