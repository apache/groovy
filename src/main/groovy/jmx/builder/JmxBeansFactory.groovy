/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package groovy.jmx.builder

import javax.management.MBeanServer

/**
 *
 * @author vladimir
 */
class JmxBeansFactory extends AbstractFactory{
    public Object newInstance(FactoryBuilderSupport builder, Object nodeName, Object nodeParam, Map nodeAttribs) {
        if (!nodeParam || !(nodeParam instanceof List)) {
            throw new JmxBuilderException("Node '${nodeName}' requires a list of object to be exported.")
        }

        JmxBuilder fsb = (JmxBuilder) builder
        MBeanServer server = (MBeanServer) fsb.getMBeanServer()
        
        def metaMaps = []
        def targets  = nodeParam
        def map

        // prepare metaMap for each object
        targets.each {target ->
            def metaMap = JmxMetaMapBuilder.buildObjectMapFrom(target)
            metaMap.server = metaMap.server ?: server
            metaMap.isMBean = JmxBuilderTools.isClassMBean(target.getClass())
            metaMaps.add(metaMap)
        }
        return metaMaps
    }

    public boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map nodeAttribs) {
        return false;
    }

    public boolean isLeaf() {
        return true;
    }

    public void onNodeCompleted(FactoryBuilderSupport builder, Object parentNode, Object thisNode) {
        JmxBuilder fsb = (JmxBuilder) builder
        MBeanServer server = (MBeanServer) fsb.getMBeanServer()
        def metaMaps = thisNode

        def regPolicy = fsb.getParentFactory()?.registrationPolicy ?: "replace"

        metaMaps.each {metaMap ->
            def registeredBean = JmxBuilderTools.registerMBeanFromMap(regPolicy, metaMap)

            // if replace, remove from parent node and re add.
            if(registeredBean && regPolicy == "replace"){
                for (Iterator i = parentNode.iterator(); i.hasNext();) {
                    def exportedBean = i.next()
                    if (exportedBean.name().equals(metaMap.jmxName)) {
                        i.remove()
                    }
                }
            }

            // only add if bean was successfully registered.
            if (parentNode != null && registeredBean) {
                parentNode.add(registeredBean)
            }
        }
    }
}

