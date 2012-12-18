afterMethodCall { mc ->
    def method = getTargetMethod(mc)
    if (isExtensionMethod(method) && method.name == 'sprintf') {
        def argList = getArguments(mc)
        if (argList && isConstantExpression(argList[0])) {
            def pattern = argList[0].text
            def codes = pattern.replaceAll(/[^%]*%([a-zA-Z]+)/, '_$1').tokenize('_')
            def args = getArgumentTypes(argList).toList().tail()
            if (args.size() != codes.size()) {
                addStaticTypeError("Found ${args.size()} parameters for sprintf call with ${codes.size()} conversion code placeholders in the format string", argList)
                return
            }
            def codeTypes = codes.collect { code ->
                switch (code) {
                    case 's': return STRING_TYPE
                    case 'd': return int_TYPE
                    case 'tF': return classNodeFor(Date)
                    default: return null
                }
            }
            if (codeTypes != args) {
                addStaticTypeError("Parameter types didn't match types expected from the format String: ", argList)
                (0..<args.size()).findAll { args[it] != codeTypes[it] }.each { n ->
                    String msg = "For placeholder ${n + 1} [%${codes[n]}] expected '${codeTypes[n].toString(false)}' but was '${args[n].toString(false)}'"
                    addStaticTypeError(msg, argList.getExpression(n + 1))
                }
            }
        }
    }
}
