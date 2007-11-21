package groovy

class EnumTest extends GroovyTestCase {
    void testValues() {
        assert Coin.values().size() == 4
        assert Coin.values().toList().sum{ it.value } == 41
    }
}

enum Coin {
    penny(1), nickel(5), dime(10), quarter(25)
    Coin(int value) { this.value = value }
    private final int value
    int getValue() { return value }
}
