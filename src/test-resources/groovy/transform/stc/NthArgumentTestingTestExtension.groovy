def expectations = [
        [0, String],
        [1, Integer],
        [2, Date]
]
afterMethodCall { call ->
    def args = getArguments(call)
    expectations.each {
        def (index, type) = it
        if (call.methodAsString in ['two', 'three'] && argTypeMatches(call, index, type)) {
            addStaticTypeError "Method [${call.methodAsString}] with matching argument found: $it", args[index]
        }
    }
}