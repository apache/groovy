package groovy.jmx.builder

class JmxBeanExportFactory extends AbstractFactory {
    // def server
    def registrationPolicy

    public Object newInstance(FactoryBuilderSupport builder, Object nodeName, Object nodeArgs, Map nodeAttribs) {
        registrationPolicy = nodeAttribs?.remove("policy") ?: nodeAttribs?.remove("regPolicy") ?: "replace"
        return []
    }

    public boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map nodeAttribs) {
        return true;
    }

    public boolean isLeaf() {
        return false
    }

}