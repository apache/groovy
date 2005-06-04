package groovy.mock.example

import groovy.mock.GroovyMock

class SandwichMakerTest extends GroovyTestCase {

    void testStuff(){

        mockCheeseSlicer = GroovyMock.newInstance()
        sandwichMaker = new SandwichMaker()
        sandwichMaker.cheeseSlicer = mockCheeseSlicer.instance

        // expectation
        mockCheeseSlicer.sliceCheese {arg -> assert arg.startsWith("ch")}

        // execute
        sandwichMaker.makeFattySandwich()

        // verify
        mockCheeseSlicer.verify()

    }

}