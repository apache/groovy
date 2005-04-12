package groovy.mock.example

class SandwichMaker {

    property cheeseSlicer

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