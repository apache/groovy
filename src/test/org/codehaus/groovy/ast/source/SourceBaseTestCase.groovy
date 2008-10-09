package org.codehaus.groovy.ast.source

import org.codehaus.groovy.control.*
import org.codehaus.groovy.control.io.*
import org.codehaus.groovy.ast.stmt.*

class SourceBaseTestCase extends GroovyTestCase {

    private classNode
    public classNode() {
      if (classNode!=null) return node
      def cu = new CompilationUnit(null,null,this.class.classLoader)
      def source = new StringReaderSource(script,cu.configuration)
      def su = cu.addSource(new SourceUnit("Script_"+this.name, source, cu.configuration, cu.classLoader, cu.errorCollector))
      cu.compile(Phases.CONVERSION)
      classNode = cu.firstClassNode
      return classNode
    }
    
    def sourceInfo(expression) {
      return [  expression.lineNumber,
                expression.columnNumber,
                expression.lastLineNumber,
                expression.lastColumnNumber 
             ]
    }
    
    def statements(String method="run") {
      def ret = classNode().getMethod(method).code
      if (ret instanceof BlockStatement) return ret.statements
      if (ret==null) return null
      return [ret]
    }
}