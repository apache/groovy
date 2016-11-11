import groovy.transform.*

class SomeContainer {
    public Object getAt(int i) {
        return "123";
    }
}

@CompileStatic
def cs() {
    List list = null;
    list?[1];

    Map map = null;
    map?[1];

    SomeContainer sc = null;
    sc?[1];
}

cs()
