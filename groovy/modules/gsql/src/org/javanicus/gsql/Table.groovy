package org.javanicus.gsql

public class Table implements Cloneable {
    property catalog
    property name
    property groovyName
    property schema
    property remarks
    property type
    private columns
    private foreignKeys
    private indexes
    
    public Table() {
        catalog = null
        name = null
        groovyName = null
        schema = null
        remarks = null
        type = null
        columns = []
        foreignKeys = []
        indexes = []
    }

    public Object clone() { // throws CloneNotSupportedException {
        result = new Table()

        result.catalog     = catalog
        result.name        = name
        result.groovyName  = groovyName
        result.schema      = schema
        result.remarks     = remarks
        result.type        = type
        result.columns     = columns.clone()
        result.foreignKeys = foreignKeys.clone()
        result.indexes     = indexes.clone()
        
        return result
    }

    public String getType() {
        return (type == null) ? "(null)" : type
    }

    public void addColumn(column) {
        columns << column
    }
    public void addAll(columns) {
        this.columns << columns
    }
    public List getColumns() {
        return columns
    }
    public Column getColumn(index) {
        return columns[index]
    }

    public void addForeignKey(foreignKey) {
        foreignKeys << foreignKey
    }
    public List getForeignKeys() {
        return foreignKeys
    }
    public ForeignKey getForeignKey(index) {
        return foreignKeys[index]
    }

    public void addIndex(Index index) {
        indexes << index
    }
    public List getIndexes() {
        return indexes
    }
    public Index getIndex(int index) {
        return (Index)indexes.get(index)
    }

    public void addUnique(Unique index) {
        addIndex(index)
    }
    public List getUniques() {
        return indexes.findAll() {it.isUnique()}
    }
    
    
    public boolean hasPrimaryKey() {
        aPrimaryKeyColumn = getColumns().find() {it.isPrimaryKey()}
        return aPrimaryKeyColumn != null
    }

    public Column findColumn(name) {
        return getColumns().find() {it.name.equalsIgnoreCase(name)}
    }

    public Index findIndex(name) {
        return getIndexes().find() {it.name.equalsIgnoreCase(name)}
    }

    public List getPrimaryKeyColumns() {
        return getColumns().findAll() {it.isPrimaryKey()}
    }

    public Column getAutoIncrementColumn() {
        return getColumns().find() {it.isAutoIncrement()}
    }
}
