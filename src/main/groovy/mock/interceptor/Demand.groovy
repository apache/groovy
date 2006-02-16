package groovy.mock.interceptor

class Demand {

    @Property List recorded = []

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
    @Property String  name
    @Property Closure behavior
    @Property Range   range
}
