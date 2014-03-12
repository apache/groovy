incompatibleReturnType { returnStmt, inferredReturnType ->
    if (inferredReturnType==STRING_TYPE) {
        handled = true
    }
}