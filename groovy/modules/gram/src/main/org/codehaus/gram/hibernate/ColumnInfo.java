package org.codehaus.gram.hibernate;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a simple POJO containing the details of a column
 *
 * @version $Revision$
 */
public class ColumnInfo {
    protected static Set primitiveTypes = createPrimitiveTypes();

    private String columnName = "";
    private String tableName = "";
    private String typeName = "";
    private String qualifiedTypeName = "";
    private String cardinality = "";
    private String foreignKey = "";
    private int length = 0;
    private boolean notNull;

    public String getCardinality() {
        return cardinality;
    }

    public void setCardinality(String cardinality) {
        this.cardinality = cardinality;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(String foreignKey) {
        this.foreignKey = foreignKey;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getQualifiedTypeName() {
        return qualifiedTypeName;
    }

    public void setQualifiedTypeName(String qualifiedTypeName) {
        this.qualifiedTypeName = qualifiedTypeName;
        int idx = qualifiedTypeName.lastIndexOf('.');
        if (idx > 0) {
            typeName = qualifiedTypeName.substring(idx + 1);
        }
        else {
            typeName = qualifiedTypeName;
        }
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public boolean isPrimitiveType() {
        return primitiveTypes.contains(getQualifiedTypeName());
    }

    protected static Set createPrimitiveTypes() {
        Set answer = new HashSet();
        answer.add("boolean");
        answer.add("byte");
        answer.add("char");
        answer.add("short");
        answer.add("int");
        answer.add("long");
        answer.add("float");
        answer.add("double");
        answer.add("java.lang.Boolean");
        answer.add("java.lang.Byte");
        answer.add("java.lang.Character");
        answer.add("java.lang.Short");
        answer.add("java.lang.Integer");
        answer.add("java.lang.Long");
        answer.add("java.lang.Float");
        answer.add("java.lang.Double");
        answer.add("java.math.BigInteger");
        answer.add("java.math.BigDecimal");
        answer.add("java.util.Date");
        answer.add("java.sql.Date");
        answer.add("java.sql.Time");
        answer.add("java.sql.Timestamp");
        return answer;
    }


}
