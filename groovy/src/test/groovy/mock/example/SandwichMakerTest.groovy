package groovy.mock.example

import groovy.mock.interceptor.MockFor

class SandwichMakerTest extends GroovyTestCase {

    void testStuff(){

        def mocker = new MockFor(CheeseSlicer.class)

        mocker.demand.slice { name ->
            assert name.startsWith("ch") 
        }

        def sandwichMaker = new SandwichMaker()

        mocker.use(sandwichMaker.cheeseSlicer) { // todo: should also work without giving the object!
            sandwichMaker.makeFattySandwich()
        }

    }

}
