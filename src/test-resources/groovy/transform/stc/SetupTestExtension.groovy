// Dummy extension that just puts the "setup" node metadata on the 'A' class node
setup {
    def cn = context.source.AST.classes.find { it.name == 'A' }
    cn.putNodeMetaData('setup', true)
}
