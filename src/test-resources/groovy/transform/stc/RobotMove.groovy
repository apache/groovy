unresolvedVariable { var ->
    if ('robot' == var.name) {
        def robotClass = lookupClassNodeFor('Robot')
        storeType(var, robotClass)
        handled = true
    }
}