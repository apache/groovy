package org.javanicus.gsql

public class Table extends GroovyObjectSupport implements Cloneable {
    property catalog
    property name
    property groovyName
    property schema
    property remarks
    property type
    private columns
    private foreignKeys
    private indexes
    
    public Table(aName) {
        catalog = null
        name = aName
        groovyName = null
        schema = null
        remarks = null
        type = null
        columns = []
        foreignKeys = []
        indexes = []
    }

    public Object clone() { // throws CloneNotSupportedException {
        result = new Table(name)

        result.catalog     = catalog
        result.name        = name
        result.groovyName  = groovyName
        result.schema      = schema
        result.remarks     = remarks
        result.type        = type
// @todo - cannot access private variables in other instances of this class                  
//        result.columns     = columns.clone()
//        result.foreignKeys = foreignKeys.clone()
//        result.indexes     = indexes.clone()
        
        return result
    }

// @todo - cannot override property getter succesfully
//    public String getType() {
//        return (type == null) ? "(null)" : type
//    }

    public void addColumn(aColumn) {
        columns << aColumn
    }
    public void addAll(someColumns) {
        this.columns << someColumns
    }
    public List getColumns() {
        return columns
    }
    public Column getColumn(idx) {
        return columns[idx]
    }

    public void addForeignKey(aForeignKey) {
        foreignKeys << aForeignKey
    }
    public List getForeignKeys() {
        return foreignKeys
    }
    public ForeignKey getForeignKey(idx) {
        return foreignKeys[idx]
    }

    public void addIndex(Index anIndex) {
        indexes << anIndex
    }
    public List getIndexes() {
        return indexes
    }
    public Index getIndex(int idx) {
        (Index)indexes.get(idx)
    }

    public void addUnique(Unique aUniqueIndex) {
        addIndex(aUniqueIndex)
    }
    public List getUniques() {
        indexes.findAll() {it.isUnique()}
    }
    
    
    public boolean hasPrimaryKey() {
        aPrimaryKeyColumn = getColumns().find() {it.isPrimaryKey()}
        return aPrimaryKeyColumn != null
    }   
    public Object getProperty(String propertyName) {
        try {
            return super.getProperty(propertyName);
        } catch (Exception e) {
            return findColumn(propertyName);
        }
    }
    public Column findColumn(aName) {
        // @todo
        // warning 'name' inside closure refers to 'this.name', not method parameter called 'name' (in groovy 1.0b6) !!!
        getColumns().find() {it.name.equalsIgnoreCase(aName)}
    }

    public Index findIndex(aName) {
        getIndexes().find() {it.name.equalsIgnoreCase(aName)}
    }

    public List getPrimaryKeyColumns() {
        getColumns().findAll() {it.isPrimaryKey()}
    }

    public Column getAutoIncrementColumn() {
        getColumns().find() {it.isAutoIncrement()}
    }
    
    public String toString() {
        "Table[name=${name};columnCount=${columns.size()}]"
    }

}
