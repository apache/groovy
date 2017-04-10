import groovy.transform.*

class SomeContainer {
    public Object getAt(int i) {
        return "123";
    }

    public void putAt(int i, Object obj) {
    }
}

def safe() {
    List list = null;
    assert null == list?[1];
    list?[1] = 'a';
    assert null == list?[1];

    Map map = null;
    assert null == map?[1];
    map?[1] = 'a';
    assert null == map?[1];

    SomeContainer sc = null;
    assert null == sc?[1];
    sc?[1] = 'a';
    assert null == sc?[1];
}
safe();

@CompileStatic
def csSafe() {
    List list = null;
    assert null == list?[1];
    list?[1] = 'a';
    assert null == list?[1];

    Map map = null;
    assert null == map?[1];
    map?[1] = 'a';
    assert null == map?[1];

    SomeContainer sc = null;
    assert null == sc?[1];
    sc?[1] = 'a';
    assert null == sc?[1];
}
csSafe();
