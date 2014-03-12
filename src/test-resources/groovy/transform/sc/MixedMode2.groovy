methodNotFound { receiver, name, argumentList, argTypes, call ->
    if (isStaticMethodCallOnClass(call, STRING_TYPE)) {
        return makeDynamic(call, buildMapType(STRING_TYPE,Integer_TYPE))
    }
    if (isStaticMethodCallOnClass(call, classNodeFor(Date))) {
        return makeDynamic(call, buildListType(Integer_TYPE))
    }
}
