// A simple extension that tries to find a matching method on the same
// receiver type, but using the lower case version of the method
methodNotFound { receiver, name, argumentList, argTypes, call ->
    def result = null
    withTypeChecker {
        def candidates = findMethod(receiver, name.toLowerCase(), argTypes)
        if (candidates && candidates.size()==1) {
            result= candidates[0]
        }
    }
    result
}
