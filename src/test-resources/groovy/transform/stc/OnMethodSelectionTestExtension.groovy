// Dummy extension that removes node metadata on LHS if a method is selected and it's 'toUpperCase'
onMethodSelection { expr, method ->
    if (method.name=='toUpperCase' && enclosingBinaryExpression) {
        enclosingBinaryExpression.putNodeMetaData('selected', true)
    }
}