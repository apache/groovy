import groovy.xml.MarkupBuilder

setup {
    debug = true
    newScope() // make sure currentScope is always not null
}

methodNotFound { receiver, name, argumentList, argTypes, call ->
    if (receiver==classNodeFor(MarkupBuilder) && argTypes[-1]==CLOSURE_TYPE) {
        // we recognized a call directly made on markupBuilder, like in
        // mkp.html { ... }
        // so we create a new "scope" so that subsequent unresolved calls are made dynamically
        newScope {
            dynamic = call
        }
        return makeDynamic(call)
    } else {
        // check if we're inside a builder
        if (currentScope.dynamic && isMethodCallExpression(call) && call.implicitThis) {
            return makeDynamic(call)
        }
    }

}

afterMethodCall { call ->
    // we need to recognize whenever we're exiting the scope of the dynamic builder
    if (call.is(currentScope.dynamic)) {
        log "Exiting scope of $call.text"
        scopeExit()
    }
}