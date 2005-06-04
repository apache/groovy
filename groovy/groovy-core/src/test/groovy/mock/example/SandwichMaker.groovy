package groovy.mock.example

class SandwichMaker {

    @Property cheeseSlicer

    void makeFattySandwich() {
        cheeseSlicer.sliceCheese("cheddar")
    }

    void makeLiteSandwich() {
        cheeseSlicer.sliceCheese("dairylea")
    }

    void makeCrappySandwich() {
        cheeseSlicer.sliceCheese("dairylea")
    }

}