package groovy.jmx.builder

import javax.management.MBeanServerConnection

class JmxBuilder extends FactoryBuilderSupport {
    private MBeanServerConnection server
    private String defaultNameDomain = "jmx.builder"
    private String defaultNameType = "ExportedObject"
    private String mode = "markup"

    public JmxBuilder() {
        registerFactories()
    }

    public JmxBuilder(MBeanServerConnection svrConnection) {
        this()
        server = svrConnection
    }

    protected void registerFactories() {
        registerFactory "export", new JmxBeanExportFactory()
        registerFactory "bean", new JmxBeanFactory()
        registerFactory "timer", new JmxTimerFactory()
        registerFactory "listener", new JmxListenerFactory()
        registerFactory "emitter", new JmxEmitterFactory()

        JmxServerConnectorFactory svrFactory = new JmxServerConnectorFactory()
        registerFactory "server", svrFactory
        registerFactory "connectorServer", svrFactory
        registerFactory "serverConnector", svrFactory

        def newClientFactory = {new JmxClientConnectorFactory()}
        registerFactory "client", newClientFactory()
        registerFactory "connector", newClientFactory()
        registerFactory "clientConnector", newClientFactory()
        registerFactory "connectorClient", newClientFactory()
    }

    public MBeanServerConnection getMBeanServer() {
        if (!server) {
            server = JmxBuilderTools.getMBeanServer()
        }
        return server
    }

    public void setDefaultJmxNameDomain(String domain) {
        this.defaultNameDomain = domain
    }

    public String getDefaultJmxNameDomain() {
        return this.defaultNameDomain
    }

    public void setDefaultJmxNameType(String type) {
        this.defaultNameType = type
    }

    public String getDefaultJmxNameType() {
        return this.defaultNameType
    }

    public void setMBeanServer(MBeanServerConnection svr) {
        server = svr
    }

    public void setMode(String mode) {
        this.mode = mode
    }

    public String getMode() {
        return mode
    }

    protected Factory resolveFactory(Object name, Map attributes, Object value) {
        Factory factory = super.resolveFactory(name, attributes, value);
        if (!factory) {
            factory = getParentFactory()?.childFactory
        }
        return factory
    }

}