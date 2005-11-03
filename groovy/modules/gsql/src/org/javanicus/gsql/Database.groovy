package org.javanicus.gsql

//@todo should extend Expando, super.getProperty() will currently always return
//        something, even if it's null. Expando could either throw GroovyRuntimeException
//        or this class could check for null return, and findTable() instead
public class Database extends GroovyObjectSupport {
    @Property name
    @Property version
    @Property List tables

    public Database(aName) {
        this.name = aName
        tables = []
    }

    public void mergeWith(Database otherDb) { //throws IllegalArgumentException {
        otherDb.tables.each() {
            if (findTable(it.name) != null) {
                //@todo throw new IllegalArgumentException("Table ${table.name} already defined in this database");
            } else {
                tables << it.clone()
            }
        }
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

    public String toString() {
        "Database[name=${name};tableCount=${tables.size()}]"
    }
}
