import groovy.transform.CompileStatic

def elvisAssignment() {
    def a = 2
    a ?= 1
    assert a == 2

    a = null
    a ?= 1
    assert a == 1

    a = null
    a ?= a ?= 1
    assert a == 1

    a = null
    assert (a ?= '2') == '2'
}
elvisAssignment();

@CompileStatic
def csElvisAssignment() {
    def a = 2
    a ?= 1
    assert a == 2

    a = null
    a ?= 1
    assert a == 1

    a = null
    a ?= a ?= 1
    assert a == 1

    a = null
    assert (a ?= '2') == '2'
}
csElvisAssignment();
