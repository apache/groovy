package gls.enums.vm5

class EnumTest extends GroovyTestCase {
    void testValues() {
        assert UsCoin.values().size() == 4
        assert UsCoin.values().toList().sum{ it.value } == 41
    }
}

enum UsCoin {
    penny(1), nickel(5), dime(10), quarter(25)
    UsCoin(int value) { this.value = value }
    private final int value
    int getValue() { value }
}
