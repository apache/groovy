methodNotFound { receiver, name, argumentList, argTypes, call ->
    return makeDynamic(call, int_TYPE)
}

unresolvedProperty { pexp ->
    makeDynamic(pexp, int_TYPE)
}

unresolvedAttribute { pexp ->
    makeDynamic(pexp, int_TYPE)
}

unresolvedVariable { var ->
    makeDynamic(var, int_TYPE)
}