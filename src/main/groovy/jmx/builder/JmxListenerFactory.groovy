package groovy.jmx.builder

import javax.management.InstanceNotFoundException
import javax.management.NotificationFilterSupport

class JmxListenerFactory extends AbstractFactory {
    public Object newInstance(FactoryBuilderSupport builder, Object nodeName, Object nodeParam, Map nodeAttribs) {
        if (nodeParam) {
            throw new JmxBuilderException("Node '${nodeName}' only supports named attributes.")
        }
        JmxBuilder fsb = (JmxBuilder) builder
        def server = fsb.getMBeanServer()
        def map = JmxMetaMapBuilder.createListenerMap(nodeAttribs);

        def broadcaster = map.get("from");
        try {
            def eventType = (String) map.get("event");
            if (!server.isRegistered(broadcaster)) {
                throw new JmxBuilderException("MBean '${broadcaster.toString()}' is not registered in server.")
            }
            if (eventType) {
                NotificationFilterSupport filter = new NotificationFilterSupport();
                filter.enableType(eventType);
                server.addNotificationListener(broadcaster, JmxEventListener.getListner(), filter, map);
            } else {
                server.addNotificationListener(broadcaster, JmxEventListener.getListner(), null, map);
            }
        } catch (InstanceNotFoundException e) {
            throw new JmxBuilderException(e);
        }

        map
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