import groovy.transform.CompileStatic

@CompileStatic
def cs() {
    assert 'a' instanceof String
    assert 'a' !instanceof Integer
    assert 1 !instanceof String
    assert null !instanceof String
    assert 1 in [1, 2]
    assert 3 !in [1, 2]
    assert 3 !in ['1', '2']
    assert '3' !in [1, 2]
    assert '3' !in ['1', '2']
    assert null !in ['1', '2']
    assert null !in [1, 2]
}

cs();