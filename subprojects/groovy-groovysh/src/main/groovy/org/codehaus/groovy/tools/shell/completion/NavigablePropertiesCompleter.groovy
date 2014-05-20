package org.codehaus.groovy.tools.shell.completion

public class NavigablePropertiesCompleter {

    /**
     * Adds navigable properties to the list of candidates if they match the prefix
     */
    public void addCompletions(Object instance, final String prefix, final Set<CharSequence> candidates) {
        if (instance == null) {
            return;
        }
        this.addIndirectObjectMembers(instance, prefix, candidates)
    }


    void addIndirectObjectMembers(Object instance, String prefix, final Set<CharSequence> candidates) {
        if (instance instanceof Map) {
            Map map = (Map) instance;
            addMapProperties(map, prefix, candidates)
        }
        if (instance instanceof Node) {
            Node node = (Node) instance;
            addNodeChildren(node, prefix, candidates)
        }
        if (instance instanceof NodeList) {
            NodeList nodeList = (NodeList) instance;
            addNodeListEntries(nodeList, prefix, candidates)
        }
    }

    void addMapProperties(Map instance, String prefix, final Set<CharSequence> candidates) {
        // key can be any Object but only Strings will be completed
        for (String key in instance.keySet().findAll {it instanceof String}) {
            // if key has no Control characters
            if (key =~ '^[^\\p{Cntrl}]+$' && key.startsWith(prefix)) {
                // if key cannot be parsed used as property name, does no start with valid char, or contains invalid char
                if (key =~ '^[^a-zA-Z_$]|[ @#%^&§()+\\-={}\\[\\]~`´<>,."\'/!?:;|\\\\]') {
                    key = key.replace('\\', '\\\\').replace('\'', '\\\'')
                    key = '\'' + key + '\''
                }
                candidates.add(key)
            }
        }
    }

    void addNodeListEntries(NodeList instance, String prefix, final Set<CharSequence> candidates) {
        for (Object member in instance) {
            addIndirectObjectMembers(member, prefix, candidates)
        }
    }

    void addNodeChildren(Node instance, String prefix, final Set<CharSequence> candidates) {
        for (Object child in instance.children()) {
            String member = ''
            if (child instanceof String) {
                member = (String) child;
            } else if (child instanceof Node) {
                member = ((Node) child).name();
            } else if (child instanceof NodeList) {
                for (node in ((NodeList)child)) {
                    addNodeChildren(node, prefix, candidates)
                }
            } else {
                continue;
            }
            if (member.startsWith(prefix)) {
                candidates.add(member);
            }
        }
    }
}
