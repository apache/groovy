package groovy.bugs

public class Groovy3465Helper {
    static func(arg) {
        assert arg instanceof Map
        assert arg.size() == 2
        assert arg.containsKey('text') && arg.containsKey('value')
        return arg
    }
}
