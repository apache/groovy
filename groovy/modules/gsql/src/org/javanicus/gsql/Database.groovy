package org.javanicus.gsql

public class Database extends GroovyObjectSupport {
    property name
    property version
    private tables

    public Database(aName) {
        this.name = aName
        tables = []
    }

    public void mergeWith(Database otherDb) { //throws IllegalArgumentException {
        otherDb.tables.each() {
            if (findTable(it.name) != null) {
                //@todo throw new IllegalArgumentException("Table ${table.name} already defined in this database");
            } else {
                addTable(it.clone())
            }
        }
    }

    public void addTable(aTable) {
        tables.add(aTable)
    }

    public List getTables() {
        return tables
    }
    
    public Object getProperty(String propertyName) {
        try {
            return super.getProperty(propertyName)
        } catch (Exception e) {
            return findTable(propertyName)
        }
    }
    public Table findTable(String aName) {
        tables.find() {it.name.equalsIgnoreCase(aName)}
    }

    public void setTable(idx, aTable) {
        addTable(aTable)
    }

    public Table getTable(idx) {
        tables.get(idx)
    }
    
    public String toString() {
        "Database[name=${name};tableCount=${tables.size()}]"
    }
}
