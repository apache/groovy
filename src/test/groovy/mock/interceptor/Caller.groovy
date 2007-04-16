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

    String callFoo1() {
        return new Collaborator().foo
    }
    String callFoo2() {
        return new Collaborator().foo
    }

    void setBar1() {
        new Collaborator().bar = "bar1"
        return
    }

    void setBar2() {
        new Collaborator().setBar("bar2")
        return
    }

}