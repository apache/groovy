# Compiler app specifications
version 1.0

author [Seagull](mailto:xSeagullx@gmail.com)

## Abstract

Application should receive path to single Groovy source file, and produce corresponding class files for it.
It's very desirable to hook at compilation process as soon, as possible, to provide most basic ast building operations and do not rewrite semantic analysis, class generation and so on.

For prototype is planed to use CompilerConfiguration and replace parser via `setPluginFactory()` method.

