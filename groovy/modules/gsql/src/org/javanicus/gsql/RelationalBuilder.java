/**
 * Helper class to construct relational database schemas
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */
package org.javanicus.gsql;

import java.util.*;
import groovy.lang.Closure;
import groovy.util.BuilderSupport;
import org.codehaus.groovy.runtime.InvokerHelper;

public class RelationalBuilder extends BuilderSupport {
    private TypeMap typeMap;
    private Map factories;
    //    private Logger log = Logger.getLogger(getClass().getName());
    public RelationalBuilder(TypeMap typeMap) {
        this.typeMap = typeMap;
        factories = new HashMap();
        factories.put("database",new DatabaseFactory());
        factories.put("table",new TableFactory());
        factories.put("column",new ColumnFactory());
    }
    
    private Map passThroughNodes = new HashMap();
    
    protected void setParent(Object parent, Object child) {
//        System.out.println("setParent(" + parent + "," + child + ")");
        
        if (child instanceof Column) {
            if (parent instanceof Table) {
                Table table = (Table)parent;
                table.getColumns().add(child);
            }
        }
        if (child instanceof Table) {
            if (parent instanceof Database) {
                Database database = (Database)parent;
                database.getTables().add(child);
            }
        }
    }

    protected void nodeCompleted(Object parent, Object node) {
//        System.out.println("nodeCompleted(" + parent + "," + node + ")");
    }

    protected Object createNode(Object name) {
//        System.out.println("createNode(name:" + name + ")");
        return name;
    }

    protected Object createNode(Object name, Object value) {
//        System.out.println("createNode(name:" + name + ",value:" + value + ")");
        return name;
    }

    protected Object createNode(Object name, Map attributes, Object value) {
//        System.out.println("createNode(name:" + name + ",attributes:" + attributes + ",value:" + value + ")");
        return name;
    }
    
    protected Object createNode(Object name, Map attributes) {
        //@todo - is this a suitable return for unrecognised node types?
        Object result = name;
//        System.out.println("createNode(name:" + name + ",attributes:" + attributes + ")");
        String nodeName = (String)attributes.get("name");
        Factory factory = (Factory)factories.get(name);
        if (factory != null) {
            result = factory.make(nodeName,attributes,null);
        }
        return result;
    }
    
    private void setAttributes(Object result, Map attributes) {
        Iterator itr = attributes.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry entry = (Map.Entry)itr.next();
            try {
                InvokerHelper.setProperty(result, (String)entry.getKey(), entry.getValue());
            }
            catch (Exception e) {
                //ignore unknown attributes
            }
        }
    }        
    
    private interface Factory {
        Object make(String nodeName, Map attributes, Object value);
    }
    private class DatabaseFactory implements Factory {
        public Object make(String nodeName, Map attributes, Object value) {
            Database database = new Database(nodeName);
            setAttributes(database,attributes);
            return database;
        }
    }
    private class TableFactory implements Factory {
        public Object make(String nodeName, Map attributes, Object value) {
            Table table = new Table(nodeName);
            setAttributes(table,attributes);
            return table;
        }
    }
    private class ColumnFactory implements Factory {
        public Object make(String nodeName, Map attributes, Object value) {
            Column column = new Column(typeMap);
            setAttributes(column,attributes);
            return column;
        }
    }

}
