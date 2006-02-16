package groovy.mock.interceptor

/**
    Helper class for testing.
    @author Dierk Koenig
*/

class Caller {
    int collaborateOne() {
        return new Collaborator().one()
    }
    int collaborateOne(int arg) {
        return new Collaborator().one( arg )
    }
    int collaborateOne(int one, two) {
        return new Collaborator().one( one, two )
    }
    int collaborateTwo() {
        return new Collaborator().two()
    }
    String collaborateJava() {
        return 'whatever'.toString()
    }

}