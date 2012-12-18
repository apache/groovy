beforeVisitClass { cn ->
    if (cn.name=='B') {
        handled = true // disables visit of this class node
    }
}
afterVisitClass { cn ->
    cn.putNodeMetaData('after', true)
}