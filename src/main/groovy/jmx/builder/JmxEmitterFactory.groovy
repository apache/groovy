package groovy.jmx.builder

import javax.management.MBeanServer
import javax.management.NotificationFilterSupport
import javax.management.ObjectName

class JmxEmitterFactory extends AbstractFactory {
    public Object newInstance(FactoryBuilderSupport builder, Object nodeName, Object nodeParam, Map nodeAttribs) {
        if (nodeParam) {
            throw new JmxBuilderException("Node '${nodeName}' only supports named attributes.")
        }

        JmxBuilder fsb = (JmxBuilder) builder
        def server = (MBeanServer) fsb.getMBeanServer()

        def emitter = new JmxEventEmitter()
        def name = getObjectName(fsb, emitter, nodeAttribs.remove("name"))
        def event = nodeAttribs.remove("event") ?: nodeAttribs.remove("type") ?: "jmx.builder.event.emitter"

        def listeners = nodeAttribs.remove("listeners") ?: nodeAttribs.remove("recipients")

        // notification filter
        NotificationFilterSupport filter = null
        if (event) {
            filter = new NotificationFilterSupport()
            filter.enableType(event)
            emitter.setEvent(event)
        }

        // build model mbean for emitter and register it
        if (server.isRegistered(name)) {
            server.unregisterMBean(name)
        }
        server.registerMBean(emitter, name)

        // register listeners
        if (listeners && !(listeners instanceof List)) {
            throw new JmxBuilderException("Listeners must be provided as a list [listner1,...,listenerN]")
        }

        listeners.each {l ->
            def listener = l
            try {
                if (listener instanceof String) {
                    listener = new ObjectName(l)
                }
                if (listener instanceof ObjectName) {
                    server.addNotificationListener(name, listener, filter, null)
                } else {
                    emitter.addNotificationListener(listener, filter, null)
                }
            } catch (e) {
                throw new JmxBuilderException(e)
            }
        }

        return new GroovyMBean(fsb.getMBeanServer(), name)
    }


    private ObjectName getObjectName(fsb, emitter, name) {
        if (name && name instanceof ObjectName) return name
        if (name && name instanceof String) return new ObjectName(name)
        if (!name) return new ObjectName("${fsb.getDefaultJmxNameDomain()}:type=Emitter,name=Emitter@${emitter.hashCode()}")
    }

    public boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map nodeAttribs) {
        return false;
    }

    public boolean isLeaf() {
        return true;
    }

    public void onNodeCompleted(FactoryBuilderSupport builder, Object parentNode, Object thisNode) {
        if (parentNode != null) {
            parentNode.add(thisNode)
        }
    }

}