// Dummy extension that just puts the "finish" node metadata on the 'A' class node
finish {
    def cn = context.source.AST.classes.find { it.name == 'A' }
    cn.putNodeMetaData('finish', true)
}
