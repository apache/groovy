incompatibleAssignment { lhsType, rhsType, expr ->
    if (lhsType == int_TYPE && rhsType==STRING_TYPE) {
        handled = true
    }
}