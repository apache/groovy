onMethodSelection { expr, mn ->
    if (mn.name == 'foo') {
        newScope {
            custom = mn
        }
    }
}

afterMethodCall { call ->
    def m = getTargetMethod(call)
    if (m && m.name == 'foo') {
        scopeExit {
            assert custom == m
            addStaticTypeError 'Scope enter and exit behave correctly', call
        }
    }
}