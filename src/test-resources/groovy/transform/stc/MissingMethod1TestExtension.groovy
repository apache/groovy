// A simple extension that says that the "TOUPPERCASE" method returns a String
methodNotFound { receiver, name, argumentList, argTypes, call ->
    if (name=='TOUPPERCASE') {
        return newMethod(name, STRING_TYPE)
    }
}
