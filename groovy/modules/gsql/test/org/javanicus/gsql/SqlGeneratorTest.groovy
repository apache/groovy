/**
 * Test to verify valid construction of default DDL
 * 
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */
package org.javanicus.gsql

import java.io.*

class SqlGeneratorTest extends GroovyTestCase {
    @Property database
    @Property sqlGenerator
              
    void setUp() {
        typeMap = new TypeMap()          
        build = new RelationalBuilder(typeMap)
        sqlGenerator = new SqlGenerator(typeMap,System.getProperty( "line.separator", "\n" ))
                  
        database = build.database(name:'genealogy') {
          table(name:'event') {
              column(name:'event_id', type:'integer', size:10, primaryKey:true, required:true)
              column(name:'description', type:'varchar', size:30)          
          }
          table(name:'individual') {
            column(name:'individual_id', type:'integer', size:10, required:true, primaryKey:true, autoIncrement:true)
            column(name:'surname', type:'varchar', size:15, required:true)
            column(name:'event_id', type:'integer', size:10)
            foreignKey(foreignTable:'event') {
                reference(local:'event_id',foreign:'event_id')
            }
            index(name:'surname_index') {
                indexColumn(name:'surname')
            }
          }
        }
    }
    
    void testGenerateDDL() {
        testWriter = new PrintWriter(new FileOutputStream("SqlGeneratorTest.sql"))
        sqlGenerator.writer = testWriter
        sqlGenerator.createDatabase(database,true)
        testWriter.flush()
   }

}
