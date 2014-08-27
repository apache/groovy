
class A {
    private void emptyList() {
        def a = []
    }

    private void list() {
        def a = [1,2 + 32, '21']
    }

    private void emptyMap() {
        def a = [:]
    }

    private void map() {
        def a = [
                key1: value,
                key2: 12,
                key3: 12 + 23]
    }

    private void mapStringKey() {
        def a = ['stringKey': value]
        a = ["gStringKey$a": value]
        a = ["gStringKey${a + a}": value]
        println(12, 123
        ,14, 15)
    }

    private void mapVariableKey() {
        def a = [(key1): value, (key2 + '12'): 12]
    }
}
