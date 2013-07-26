// A dummy extension that says that every undefined variable is of type String
// but doesn't set the handled=true flag
unresolvedVariable { var ->
    if (isDynamic(var)) {
        storeType(var, STRING_TYPE)
    }
}