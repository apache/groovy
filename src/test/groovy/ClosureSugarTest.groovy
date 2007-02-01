package groovy

class ClosureSugarTest extends GroovyTestCase {

    def count;

    void testClosureSugar() {
        count = 11;

        sugar {
             count = 20;
        }

        assert count == 20;
    }

    void testMixedClosureSugar() {
        def count = 11;

        mixedSugar (5){a->
             count = count + a;
        }

        assert count == 16;

    }

    def mixedSugar(incrBy, Closure closure) {
        closure.call( incrBy ); 
    }

    def sugar(Closure closure) {
        closure.call();
    }
}
