import java.util.Arrays

public class ListFilesManualTest { 

    static void main(args) {
        println("Called main with ${args}")
        listFiles(Arrays.asList(args))
    }
    
    static String getPath(file) {
        return file.absolutePath
    }

    static void listFiles(dirs) {
        println("called with ${dirs}")
        
        for(dir in dirs) {
            println("dir: ${dir}")
            
	        def files = new java.io.File(dir).listFiles()

    	    for (f in files) {
        	    println(getPath(f))
        	} 
        }
    }
}