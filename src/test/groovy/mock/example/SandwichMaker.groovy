package groovy.mock.example

class SandwichMaker {

    def cheeseSlicer = new CheeseSlicer()

    void makeFattySandwich() {
        cheeseSlicer.slice("cheddar")
    }

}