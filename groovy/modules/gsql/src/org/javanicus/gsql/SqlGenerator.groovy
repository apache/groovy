/**
 * Generates generic SQL for Relational Structures
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 * 
 * ported from "commons-sql" [SqlBuilder.java] - revision 1.14
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author John Marshall/Connectria
 * 
 */
package org.javanicus.gsql

public class SqlGenerator {
    property lineSeparator
              
    /** The current Writer used to output the SQL to */
    property writer
    
    /** The indentation used to indent commands */
    property indent
    
    /** Whether or not primary key constraints are embedded inside the create table statement, the default is true */
    property primaryKeyEmbedded
    
    /** Whether or not foreign key constraints are embedded inside the create table statement */
    property foreignKeysEmbedded
              
    /** Whether or not indexes are embedded inside the create table statement */
    property indexesEmbedded
    
    /** Should foreign key constraints be explicitly named */
    property foreignKeyConstraintsNamed
    
    /** Is an ALTER TABLE needed to drop indexes? */
    property alterTableForDrop
    
    /** A counter used to count the constraints */
    private counter
              
    /** type mappings */
    private typeMap
              
//@todo jrr
//    public SqlGenerator() {
//        this(new TypeMap(),System.getProperty( "line.separator", "\n" ))
//    }
    
    public SqlGenerator(TypeMap typeMap,String lineSeparator) {
        this.typeMap = typeMap
        this.lineSeparator = lineSeparator
        indent = "    "
        primaryKeyEmbedded = true          
    }
    
    /**
     * Outputs the DDL required to drop and recreate the database 
     */
//@todo jrr remove
//    public void createDatabase(Database database) { //@todo throws IOException {
//        createDatabase(database, true)
//    }
    
    /**
     * Outputs the DDL required to drop and recreate the database 
     */
    public void createDatabase(database, shouldDropTable) { //@todo throws IOException {
        // lets drop the tables in reverse order as its less likely to cause
        // problems with referential constraints
        if (shouldDropTable) {
            database.tables.reverseEach {
                dropTable(it)
            }
        }
        for (table in database.tables) {
            tableComment(table)
            createTable(table)
        }
//@todo <-- bug cannot see createTable(it) inside closure????        
//        database.tables.each {
//            tableComment(it)
//            createTable(it)
//        }
        // we're writing the foreignkeys last to ensure that all referenced tables are already defined
        for (table in database.tables) {
            createExternalForeignKey(table)
        }
    }
    
    /**
      * Outputs the DDL required to drop the database 
      */
    public void dropDatabase(Database database) { //@todo throws IOException {
        // lets drop the tables in reverse order
        database.tables.reverseEach {
            tableComment(it)
            dropTable(it)
        }
    }
    
    /** 
      * Outputs a comment for the table
      */
    public void tableComment(table) { //@todo throws IOException {
        wprintComment("-----------------------------------------------------------------------")
        wprintComment(table.name)
        wprintComment("-----------------------------------------------------------------------")
        wprintln()
    }
    
    /**
      * Outputs the DDL to drop the table
      */
    public void dropTable(Table table) { //@todo throws IOException {
        wprint("drop table ${table.name}")
        wprintEndOfStatement()
    }
    
    /** 
      * Outputs the DDL to create the table along with any non-external constraints
      */
    public void createTable(table) { //@todo throws IOException {
        wprintln("create table ${table.name} (")
        
        writeColumnTypes(table)
        
        if (primaryKeyEmbedded) {
            writePrimaryKeys(table)
        }
//@todo jrr        if (foreignKeysEmbedded) {
//            writeForeignKeys(table)
//        }
        if (indexesEmbedded) {
            writeEmbeddedIndexes(table)
        }
        wprintln()
        wprint(")")
        wprintEndOfStatement()
        
        if (primaryKeyEmbedded) {
            writePrimaryKeysAlterTable(table)
        }
        if (!indexesEmbedded) {
            writeIndexes(table)
        }
    }
    /** 
      * Creates an external foreignkey definition.
      */
    public void createExternalForeignKey(Table table) { // @todo throws IOException {
        if (!foreignKeysEmbedded) {
            writeForeignKeysAlterTable(table)
        }
    }
    /**
     * Helper method to determine if two column specifications represent
     * different types.  Type, nullability, size, scale, default value,
     * and precision radix are the attributes checked.  Currently default
     * values are compared where null and empty string are considered equal.
     * See comments in the method body for explanation.
     *
     *
     * @param first First column to compare
     * @param second Second column to compare
     * @return true if the columns differ
     */
    public boolean columnsDiffer(desired,current) {
        result = false

        //The createColumn method leaves off the default clause if column.getDefaultValue()
        //is null.  mySQL interprets this as a default of "" or 0, and thus the columns
        //are always different according to this method.  alterDatabase will generate
        //an alter statement for the column, but it will be the exact same definition
        //as before.  In order to avoid this situation I am ignoring the comparison
        //if the desired default is null.  In order to "un-default" a column you'll
        //have to have a default="" or default="0" in the schema xml.
        //If this is bad for other databases, it is recommended that the createColumn
        //method use a "DEFAULT NULL" statement if that is what is needed.
        //A good way to get this would be to require a defaultValue="<NULL>" in the
        //schema xml if you really want null and not just unspecified.
        desiredDefault = desired.defaultValue
        currentDefault = current.defaultValue
        defaultsEqual = (desiredDefault == null || desiredDefault.equals(currentDefault))
        sizeMatters = (desired.size != null)
        if ( desired.typeCode != current.typeCode ||
                desired.required != current.required ||
                (sizeMatters && desired.size != current.size) ||
                desired.scale != current.scale ||
                !defaultsEqual ||
                desired.precisionRadix != current.precisionRadix ) {
            result = true
        }
        return result
    }
    /**
      * Determines whether this database requires the specification of NULL as the default value.
      *  
      * @return Whether the database requires NULL for the default value
      */
    boolean requiresNullAsDefault() {
        return false
    }
    
    /** 
      * Outputs the DDL to add a column to a table.
      */
    public void createColumn(Table table, Column column) { //@todo throws IOException {
        //see comments in columnsDiffer about null/"" defaults
        wprint("${column.name} ${getSqlType(column)} ")
        
        if (column.defaultValue != null) {
            wprint("DEFAULT '${column.defaultValue}' ")
        }
        if (column.required) {
            wprintNotNullable()
        } else if (requiresNullAsDefault()) {
            wprintNullable()
        }
        wprint(" ")
        if (column.autoIncrement) {
            wprintAutoIncrementColumn(table, column)
        }
    }
    
    // Implementation methods
    //-------------------------------------------------------------------------                
    
    /**
      * @return true if we should generate a primary key constraint for the given
      *  primary key columns. By default if there are no primary keys or the column(s) are 
      *  all auto increment (identity) columns then there is no need to generate a primary key 
      *  constraint.
      */
    boolean shouldGeneratePrimaryKeys(List primaryKeyColumns) {
        return primaryKeyColumns.any { !it.autoIncrement }
    }
  /**
    * Writes the column types for a table 
   */
    void writeColumnTypes(Table table) { // @todo throws IOException {
        first = true
        for (column in table.columns) {
            if (first) { first = false } else { wprintln(", ")}
            wprintIndent()
            createColumn(table, column)
        }
    }
   /**
     * @return the full SQL type string including the size
     */
    String getSqlType(Column column) {
        nativeType = getNativeType(column)
        sqlType = new StringBuffer()          
        if (nativeType != null) {
            sqlType.append(nativeType)
            if ( column.size != null ) {
                sqlType.append(" (${column.size}")
                if ( typeMap.isDecimalType(column.type) ){
                    sqlType.append(",${column.scale}")
                }
                sqlType.append(")")
            }
        }
        return sqlType.toString()
    }
   /**
     * Writes the primary key constraints inside a create table () clause.
    */
    void writePrimaryKeys(Table table) {// @todo throws IOException {
    //@todo protected
        
        if (table.primaryKeyColumns.size() > 0 && shouldGeneratePrimaryKeys(table.primaryKeyColumns)) {
            wprintln(",")
            wprintIndent()
            writePrimaryKeyStatement(table.primaryKeyColumns)
        }
    }
    /**
      * Writes the primary key constraints as an AlterTable clause.
      */
    void writePrimaryKeysAlterTable(table) { //@todo throws IOException {
    //@todo protected
        if (table.primaryKeyColumns.size() > 0 && shouldGeneratePrimaryKeys(table.primaryKeyColumns)) {
            wprintln("ALTER TABLE ${table.name}")
            wprintIndent()
            wprintln("ADD CONSTRAINT ${table.name}_PK")
            writePrimaryKeyStatement(table.primaryKeyColumns);
            wprintEndOfStatement()
            wprintln()
        }
    }
    /**
      * Writes the 'PRIMARY KEY(A,B,...,N)' statement
      */
        
     void writePrimaryKeyStatement(primaryKeyColumns) {//@todo throws IOException {
     //@todo protected
        wprint("PRIMARY KEY (")
        first = true
        for (column in primaryKeyColumns) {
            if (first) { first = false } else { wprint(", ")}
            wprint(column.name)
        }
         wprint(")")
    }
        
   /**
     * Writes the foreign key constraints inside a create table () clause.
    */
    // @todo jrr this whole method...
    /*    void writeForeignKeys(table) { //@todo throws IOException {
    //@todo protected
        for (key in table.foreignKeys) {
            if (key.foreignTable == null) {
                //todo log.warn( "Foreign key table is null for key: ${key}")
            } else {
                wprintln(",")
                wprintIndent()
                if (foreignKeyConstraintsNamed) {
                    wprint("CONSTRAINT ${table.name}_FK_${++counter} ")
                }
                wprint("FOREIGN KEY (")
                writeLocalReferences(key)
                wprintln(")")
                wprintIndent()
                wprint("REFERENCES ${key.foreignTable} (")
                writeForeignReferences(key)
                wprintln(")")
            }
        }
    }*/
   /**
     * Writes the foreign key constraints as an AlterTable clause.
     */
   void writeForeignKeysAlterTable(Table table) { //@todo throws IOException {
   //@todo protected     
        counter = 0
        for (key in table.foreignKeys) {
//@todo jrr            writeForeignKeyAlterTable(table,key)
        }
    }
        
//    void writeForeignKeyAlterTable( Table table, ForeignKey key ) {//@todo throws IOException {
//    //@todo protected
//        if (key.foreignTable == null) {
//            //@todo log.warn( "Foreign key table is null for key: ${key}")
//        } else {
//            wprintln("ALTER TABLE ${table.name}")
//            wprintIndent()
//            wprint("ADD CONSTRAINT ${table.name}_FK_${++counter}  FOREIGN KEY (")
//            writeLocalReferences(key)
//            wprintln(")")
//            wprintIndent()
//            wprint("REFERENCES ${key.foreignTable} (")
//            writeForeignReferences(key)
//            wprintln(")")
//            wprintEndOfStatement()
//                  
//        }
//    }
   /**
     * Writes the indexes.
     */
   void writeIndexes(Table table) { //@todo throws IOException{
   //@todo protected     
       for (index in table.indexes) {
           writeIndex(table,index)
       }
    }
    
   /**
     * Writes one index for a table
    */
    void writeIndex( table, index ) { //@todo throws IOException {
    //@todo protected
        if (index.name == null) {
            //@todo log.warn( "Index Name is null for index: ${index}")
        } else {
            wprint("CREATE")
            if ( index.unique ) {
                wprint( " UNIQUE" )
            }
            wprint(" INDEX ${index.name} ON ${table.name} (")
            first = true          
                      
            for (column in index.indexColumns) {
                if (first) { first = false } else { wprint(", ")}
                wprint(column.name)
            }
            wprint(")")
            wprintEndOfStatement()
                  }
    }
   /**
     * Writes the indexes embedded within the create table statement. not
     * yet implemented
    */
    void writeEmbeddedIndexes(table) { // @todo throws IOException 
        //@todo protected
    }
    
   /**
     * Writes a list of local references for the given key
     */
   void writeLocalReferences(ForeignKey key) { //@todo throws IOException {
   //@todo protected     
        first = true
        for (reference in key.references) {
            if (first) { first = false } else { wprint(", ")}                    
            wprint(reference.local)
        }
    }
   /**
     * Writes a list of foreign references for the given key
    */
    void writeForeignReferences(ForeignKey key) { //@todo throws IOException {
    //@todo protected
        first = true
        for (reference in key.references) {
            if (first) { first = false } else { wprint(", ")}                    
            wprint(reference.foreign)
        }
    }
   /**
     * Returns the string that denotes a comment if put at the beginning of a line.
     *  
     * @return The comment prefix
     */
    String getCommentPrefix() {
        return "--"
    }
    
   /**
     * Prints an SQL comment to the current stream
     */
    void wprintComment(String text) {//@todo throws IOException { 
    //@todo protected    
        wprint(getCommentPrefix())
        
        // MySql insists on a space after the first 2 dashes.
        // http://www.mysql.com/documentation/mysql/bychapter/manual_Reference.html#Comments
        // dunno if this is a common thing
        wprint(" ")
        wprintln( text )
    }
    
   /**
     * Prints that a column is nullable 
    */
    void wprintNullable() {//@todo throws IOException {
    //@todo protected
        wprint("NULL")
    }
    
   /**
     * Prints that a column is not nullable
     */
    void wprintNotNullable() {//@todo throws IOException {
    //@todo protected    
        wprint("NOT NULL");
    }

   /** 
     * Prints the end of statement text, which is typically a semi colon followed by 
     * a carriage return
    */
    void wprintEndOfStatement() {//@todo throws IOException {
    //@todo protected
        wprintln(";")
        wprintln()
    }

    /** 
     * Prints a new line
     */
    void wprintln() {//@todo throws IOException {
    //@todo protected
        wprint( lineSeparator )
    }

    /**
     * Prints some text
     */
    void wprint(text) {//@todo throws IOException {
    //@todo protected    
        writer.write(text)
    }
    
    /**
     * Prints some text then a newline
     */
    void wprintln(text) {//@todo throws IOException {
    //@todo protected
        wprint(text)
        wprintln()
    }

    /**
     * Prints the indentation used to indent SQL
     */
    void wprintIndent() {//@todo throws IOException {
    //@todo protected    
        wprint(getIndent())
    }

    /**
     * Outputs the fact that this column is an auto increment column.
     */ 
    void wprintAutoIncrementColumn(table, column) {//@todo throws IOException {
    //@todo protected
        wprint( "IDENTITY" )
    }

    String getNativeType(Column column){
        return column.type
    }


    /**
     * Generates the DDL to modify an existing database so the schema matches
     * the current specified database schema.  Drops and modifications will
     * not be made.
     *
     * @param desiredDb The desired database schema
     * @param cn A connection to the existing database that should be modified
     *
     * @throws IOException if the ddl cannot be output
     * @throws SQLException if there is an error reading the current schema
     */
    void alterDatabase(Database desiredDb, cn) {//@todo throws IOException, SQLException {
        alterDatabase( desiredDb, cn, false, false )
    }

    /**
     * Generates the DDL to modify an existing database so the schema matches
     * the current specified database schema.
     *
     * @param desiredDb The desired database schema
     * @param cn A connection to the existing database that should be modified
     * @param doDrops true if columns and indexes should be dropped, false if
     *      just a message should be output
     * @param modifyColumns true if columns should be altered for datatype, size, etc.,
     *      false if just a message should be output
     *
     * @throws IOException if the ddl cannot be output
     * @throws SQLException if there is an error reading the current schema
     */
    void alterDatabase(desiredDb, cn,doDrops, modifyColumns) {//@todo throws IOException, SQLException {
        //        currentDb      = new JdbcModelReader(cn).database
        //@todo line below is just test
        currentDb = new Database("wibble")
                  
        deferredTables = []
        for (desiredTable in desiredDb.tables) {
            currentTable = currentDb.findTable(desiredTable.name);
//took out because if there were no changes to be made the execution had
//errors because it tries to execute the comments as a statement
//            tableComment(desiredTable)
            if ( currentTable == null ) {
                //@todo log.info( "creating table ${desiredTable.name}");
                createTable( desiredTable )
                // we're deferring foreignkey generation
                deferredTables << desiredTable
            } else {
                //add any columns, indices, or constraints
                for (desiredColumn in desiredTable.columns) {
                    // ??? can we do currentTable.${desiredColumn.name} instead ????
                    currentColumn = currentTable.findColumn(desiredColumn.name)
                    if ( null == currentColumn ) {
                        //@todo log.info( "creating column ${desiredTable.name}.${desiredColumn.getName()}")
                        alterColumn( desiredTable, desiredColumn, true )
                    } else if ( columnsDiffer( desiredColumn, currentColumn ) ) {
                        if ( modifyColumns ) {
                            //@todo log.info( "altering column ${desiredTable.name}.${desiredColumn.name}" )
                            //@todo log.info( "  desiredColumn=${desiredColumn.toStringAll()}" )
                            //@todo log.info( "  currentColumn=${currentColumn.toStringAll()}" )
                            alterColumn( desiredTable, desiredColumn, false )
                        } else {
                            String text = "Column ${currentColumn.name} in table ${currentTable.name} differs from current specification"
                            //@todo log.info( text )
                            wprintComment( text )
                        }
                    }
                } //for columns

                //@todo add constraints here...

                //hmm, m-w.com says indices and indexes are both okay
                //@todo should we check the index fields for differences?
                for (desiredIndex in desiredTable.indexes) {
                    currentIndex = currentTable.findIndex(desiredIndex.name)
                    if ( null == currentIndex ) {
                        //@todo log.info( "creating index ${desiredTable.name}.${desiredIndex.name}" )
                        writeIndex( desiredTable, desiredIndex )
                    }
                }

                // Drops ///////////////////////
                //@todo drop constraints - probably need names on them for this

                //do any drops of columns
                for (currentColumn in currentTable.columns) {
                    desiredColumn = desiredTable.findColumn(currentColumn.name)
                    if ( null == desiredColumn ) {
                        if ( doDrops ) {
                            //@todo log.info( "dropping column ${currentTable.name}.${currentColumn.name}" )
                            dropColumn( currentTable, currentColumn )
                        } else {
                            text = "Column ${currentColumn.name} can be dropped from table ${currentTable.name}"
                            //@todo log.info( text )
                            wprintComment( text )
                        }
                    }
                } //for columns

                //drop indexes
                for (currentIndex in currentTable.indexes) {
                    desiredIndex = desiredTable.findIndex(currentIndex.name)
                    if ( null == desiredIndex ) {
                        //make sure this isn't the primary key index (mySQL reports this at least)
                        isPk = true
                        for (ic in currentIndex.indexColumns) {
                            c = currentTable.findColumn( ic.name )
                            if ( !c.primaryKey ) {
                                isPk = false
                                break
//@todo - does groovy break out of 'new' for loops or closures? 
                            }
                        }

                        if ( !isPk ) {
                            //@todo log.info( "dropping non-primary index ${currentTable.name}.${currentIndex.name}" )
                            dropIndex( currentTable, currentIndex )
                        }
                    }
                }

            } //table exists?
        } //for tables create

        // generating deferred foreignkeys
        for (table in deferredTables) {
            createExternalForeignKey(table)
        }
        

        //check for table drops
        for (currentTable in currentDb.tables) {
            desiredTable = desiredDb.findTable(currentTable.name)

            if ( desiredTable == null ) {
                if ( doDrops ) {
                    //@todo log.info( "dropping table ${currentTable.name}" )
                    dropTable( currentTable )
                } else {
                    text = "Table ${currentTable.name} can be dropped"
                    //@todo log.info( text )
                    wprintComment( text )
                }
            }

        } //for tables drops
    }

    /**
     * Generates the alter statement to add or modify a single column on a table.
     *
     * @param table The table the index is on
     * @param column The column to drop
     * @param add true if the column is new, false if it is to be changed
     *
     * @throws IOException if the statement cannot be written
     */
    void alterColumn( table, column, add ) {//@todo throws IOException {
        writeAlterHeader( table )
        wprint( add ? "ADD " : "MODIFY " )
        createColumn( table, column )
        wprintEndOfStatement()
    }

    /**
     * Generates the statement to drop an column from a table.
     *
     * @param table The table the index is on
     * @param column The column to drop
     *
     * @throws IOException if the statement cannot be written
     */
    void dropColumn( Table table, Column column ) {//@todo throws IOException {
        writeAlterHeader( table )
        wprint( "DROP COLUMN ${column.getName}" )
        wprintEndOfStatement()
    }

   /**
     * Generates the first part of the ALTER TABLE statement including the
     * table name.
     *
     * @param table The table being altered
     *
     * @throws IOException if the statement cannot be written
     */
    void writeAlterHeader( Table table ) {//@todo throws IOException {
    //@todo protected    
        wprintln("ALTER TABLE ${table.name}")
        wprintIndent()
    }
   /**
     * Generates the statement to drop an index from the database.  The
     * <code>alterTableForDrop</code> property is checked to determine what
     * style of drop is generated.
     *
     * @param table The table the index is on
     * @param index The index to drop
     *
     * @throws IOException if the statement cannot be written
     *
     * @see SqlGenerator#useAlterTableForDrop
     */
    void dropIndex( Table table, Index index ) {//@todo throws IOException {
        if ( getAlterTableForDrop() ) {
            writeAlterHeader( table )
        }
        wprint( "DROP INDEX ${index.name}")
        if ( !getAlterTableForDrop() ) {
            wprint( " ON ${table.name}" )
        }
        wprintEndOfStatement()
    }

//used to check for code to be changed when changing signatures
//protected final void wprintAutoIncrementColumn() throws IOException {};
//protected final void createColumn(Column column) throws IOException {};
        
}
