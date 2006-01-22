package groovy.util

class FileNameByRegexFinder implements IFileNameFinder{

   List getFileNames(String basedir, String pattern){
      def result = []
      new File(basedir).eachFileRecurse {
          if (it.path =~ pattern) result << it.absolutePath
      }
      return result
   }
}