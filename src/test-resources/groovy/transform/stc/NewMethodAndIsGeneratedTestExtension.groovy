setup {
    newScope()
    currentScope.m1 = newMethod('foo', String)
    currentScope.m2 = newMethod('bar') {
        int_TYPE
    }
}
finish {
    def m1 = currentScope.m1
    def m2 = currentScope.m2
    assert isGenerated(m1)
    assert isGenerated(m2)
    assert m1.returnType == STRING_TYPE
    assert m2.returnType == int_TYPE
    addStaticTypeError 'Extension was executed properly', context.source.AST.classes[0]
}
