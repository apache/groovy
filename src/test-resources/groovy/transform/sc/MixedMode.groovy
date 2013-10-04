methodNotFound { receiver, name, argumentList, argTypes, call ->
    return makeDynamic(call, int_TYPE)
}

unresolvedProperty { pexp ->
    def lhs = getType(pexp.objectExpression)
    if (lhs==classNodeFor(MetaClass)) {
        makeDynamic(pexp, CLOSURE_TYPE)
    } else {
        makeDynamic(pexp, int_TYPE)
    }
}

unresolvedAttribute { pexp ->
    makeDynamic(pexp, int_TYPE)
}

unresolvedVariable { var ->
    makeDynamic(var, int_TYPE)
}