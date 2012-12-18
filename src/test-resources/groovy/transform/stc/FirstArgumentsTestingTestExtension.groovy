afterMethodCall { call ->
    if (firstArgTypesMatches(call, [String, Integer] as Class[])) {
        addStaticTypeError "Method [${call.methodAsString}] with matching arguments found: ${getArguments(call).expressions.size()}", call
    }
}