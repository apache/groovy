class ClosureSugarTest
	extends GroovyTestCase
{
    property count;

    void testClosureSugar()
    {
        count = 11;

        sugar {
             count = 20;
        }

        assert count == 20;
    }

    void testMixedClosureSugar()
    {
        count = 11;

        mixedSugar (5){a|
             count = count + a;
        }

        assert count == 16;

    }

    mixedSugar(incrBy, Closure closure)
    {
        closure.call( incrBy ); 
    }

    sugar(Closure closure)
    {
        closure.call();
    }
}
