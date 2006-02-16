package groovy.mock.example

class SandwichMaker {

    @Property cheeseSlicer = new CheeseSlicer()

    void makeFattySandwich() {
        cheeseSlicer.slice("cheddar")
    }

}