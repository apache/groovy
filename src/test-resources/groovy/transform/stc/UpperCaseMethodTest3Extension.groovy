// This test extension checks that method definitions are not all uppercase
afterVisitMethod { mn ->
    def name = mn.name
    if (name == name.toUpperCase()) {
        addStaticTypeError('Defining method which is all uppercase is not allowed', mn)
    }
}