package groovy.mock.example

class CheeseSlicer {

    void slice(String name) {
        throw new RuntimeException('whatever nasty behavior that needs to be mocked...')
    }

    void coffeeBreak(String name) {
        // dum didum didum *slurp*, *spill*
    }

}