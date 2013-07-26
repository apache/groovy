
def expectations = [
        concat: [String],
        zero: [],
        two: [String, Integer],
        three: [String, Integer, Date]
]

afterMethodCall { call ->
    expectations.each { name, expectation ->
        if (call.methodAsString == name && argTypesMatches(call, expectation as Class[])) {
            addStaticTypeError "Method [$name] with matching arguments found: ${expectation.size()}", call
        }
    }
}