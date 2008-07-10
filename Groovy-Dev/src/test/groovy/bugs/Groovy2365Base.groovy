/**
 * Created by IntelliJ IDEA.
 * User: applerestore
 * Date: Dec 11, 2007
 * Time: 3:29:40 PM
 * To change this template use File | Settings | File Templates.
 */
package groovy.bugs

class Groovy2365Base extends GroovyTestCase {

    protected String createData () {

        File dir = createTempDir("groovy-src-", "-src")
        assertNotNull dir

        def fileList =  [
          "Util.groovy" : """

          class Util {
            static String NAME = "Util accessed"
          }
    """,

         "Script1.groovy" : """
         import Util

         println "Script1 \${Util.NAME}"
    """,

    "Script2.groovy" : """
                import Util

                println "Script2 \${Util.NAME}"
           """
                ].collect {
            name, text ->
              File file = new File(dir, name)
              file.write text
              file
         }

         return dir.absolutePath
    }

    private File createTempDir (prefix, suffix) {
        File tempFile = File.createTempFile(prefix, suffix);
        tempFile.delete();
        tempFile.mkdirs();
        tempFile.deleteOnExit()
        return tempFile;
    }

}
