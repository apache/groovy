// Used to check that GROOVY-6047 no longer throws an NPE
methodNotFound { receiver, name, argumentList, argTypes, call ->
    if (name=='elems') {
        typeCheckingVisitor.findMethodsWithGenerated(receiver, 'elements')
    }
}
