// This test extension checks that method calls are not all uppercase
beforeMethodCall { call ->
    def name = call.methodAsString
    if (name == name.toUpperCase()) {
        addStaticTypeError('Calling a method which is all uppercase is not allowed', call)
    }
}