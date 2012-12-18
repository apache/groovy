// A simple extension that says that every dynamic variable is of type String
unresolvedVariable { var ->
    if (isDynamic(var)) {
        storeType(var, STRING_TYPE)
        handled = true
    }
}