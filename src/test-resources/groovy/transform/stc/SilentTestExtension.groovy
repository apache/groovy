// A very simple extension that will silently drop all errors thrown by the type checker
setup {
    context.pushErrorCollector()
}

finish {
    def ec = context.popErrorCollector()
    assert ec.errors.size() > 0
}