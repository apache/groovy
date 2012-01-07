package groovy.util;

import java.util.Map;

class CustomNode extends Node {
    CustomNode(Node parent, Object name, Map attributes, NodeList children) {
        super(parent, name, attributes, children);
    }
}
