package org.javanicus.gsql

public class Index implements Cloneable {
    property name
    property List indexColumns
        
    public Index() {
        indexColumns = []
    }
        
    public Object clone() { //@todo throws CloneNotSupportedException {
        result = new Index()
            
        result.name         = name
        result.indexColumns = indexColumns.clone()
                  
        return result
    }
}
