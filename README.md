# Antlrv4 grammar for Groovy language.

__Google Summer of Code project.__

I'd like to rewrite Groovy's grammar in Antlr v4 terms and write a parser, which using generated one constructs Abstract Syntax Tree for Groovy.
As a part of this work I have planned to improve Groovy Console AST browser because it can help me a lot with research phase.
Also I can provide a new high-level API for AST transforms.

For this prototype is planed to use CompilerConfiguration and replace parser via `setPluginFactory()` method.

`com.xseagullx.groovy.gsoc.Main` class allows parse groovy source file and get `org.codehaus.groovy.ast.ModuleNode` via `process` method and compiling it to *.class file via `compile` method. 


CI reports are available here. [TeamCity](http://xseagullx.com:8080/TeamCity-8.1.1/?guest=1)
