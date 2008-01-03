package groovy.bugs

/**
 * @version $Revision$
 */
class WriteOnlyPropertyBug extends GroovyTestCase {
    void testSettingWriteOnlyProperty() {
        def c = new WriteOnlyBean()
        c.writeOnlyProperty = 'x' + 'y'
        assert c.thingValue() == 'xy'
    }
}

class WriteOnlyBean {
    private thing = null
    public setWriteOnlyProperty(String s) {
        thing = s
    }
    public thingValue() {
        thing
    }
}
