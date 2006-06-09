package groovy.mock.interceptor

/**
    The object that registers method calls on it for the use with Mocks and Stubs.
    For each call a CallSpec object is added to the recorded list.
    @author Dierk Koenig
*/

class Demand {

    def List recorded = []

    Object invokeMethod(String methodName, Object args) {
        def range = 1..1
        if (args[0] instanceof IntRange) {
            range = args[0]
            if (range.reverse) throw new IllegalArgumentException('Reverse ranges not supported.')
        }
        if (args[-1] instanceof Closure) {
            recorded << new CallSpec(name:methodName, behavior:args[-1], range:range)
        }
    }
}

class CallSpec {
    String  name
    Closure behavior
    Range   range
}
