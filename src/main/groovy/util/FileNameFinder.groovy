package groovy.util

class FileNameFinder implements IFileNameFinder{

   List getFileNames(String basedir, String pattern){
      def ant = new AntBuilder()
      def scanner = ant.fileScanner {
          fileset(dir:basedir, includes:pattern)
      }
      def fls = []
      for(f in scanner){
	      fls << f.getAbsolutePath()
      }
      return fls
   }
}