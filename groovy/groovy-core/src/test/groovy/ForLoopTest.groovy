class ForLoopTest extends GroovyTestCase {

	property x
	
    void testRange() {
        x = 0

        for ( i in 0..10 ) {
            x = x + i
        }

        assert x == 45
    }

    void testList() {
        x = 0
		
        for ( i in [0, 1, 2, 3, 4] ) {
            x = x + i
        }

        assert x == 10
    }

}
