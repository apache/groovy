#!/bin/sh
#
# script to make a runtime jar out of antlr distro
#
# usage:  make-antlr-runtime.sh
# (when executed in a directory containing the antlr-2.x.x.jar
# (pops result in antlr-runtime.jar)
#
# @author "Jeremy Rayner"<groovy@ross-rayner.com>

jar xvf antlr-2.7.5.jar

mkdir antlr-runtime
mkdir antlr-runtime/antlr
mkdir antlr-runtime/antlr/collections
mkdir antlr-runtime/antlr/debug
mkdir antlr-runtime/antlr/debug/misc

cp antlr/ANTLRException.class antlr-runtime/antlr
cp antlr/ANTLRHashString.class antlr-runtime/antlr
cp antlr/ANTLRStringBuffer.class antlr-runtime/antlr
cp antlr/ASTFactory.class antlr-runtime/antlr
cp antlr/ASTPair.class antlr-runtime/antlr
cp antlr/BaseAST.class antlr-runtime/antlr
cp antlr/ByteBuffer.class antlr-runtime/antlr
cp antlr/CharBuffer.class antlr-runtime/antlr
cp antlr/CharQueue.class antlr-runtime/antlr
cp antlr/CharScanner.class antlr-runtime/antlr
cp antlr/CharStreamException.class antlr-runtime/antlr
cp antlr/CharStreamIOException.class antlr-runtime/antlr
cp antlr/CommonAST.class antlr-runtime/antlr
cp antlr/CommonToken.class antlr-runtime/antlr
cp antlr/InputBuffer.class antlr-runtime/antlr
cp antlr/LLkParser.class antlr-runtime/antlr
cp antlr/LexerSharedInputState.class antlr-runtime/antlr
cp antlr/MismatchedCharException.class antlr-runtime/antlr
cp antlr/MismatchedTokenException.class antlr-runtime/antlr
cp antlr/NoViableAltException.class antlr-runtime/antlr
cp antlr/NoViableAltForCharException.class antlr-runtime/antlr
cp antlr/ParserSharedInputState.class antlr-runtime/antlr
cp antlr/Parser.class antlr-runtime/antlr
cp antlr/RecognitionException.class antlr-runtime/antlr
cp antlr/SemanticException.class antlr-runtime/antlr
cp antlr/StringUtils.class antlr-runtime/antlr
cp antlr/TokenBuffer.class antlr-runtime/antlr
cp antlr/TokenQueue.class antlr-runtime/antlr
cp antlr/TokenStreamException.class antlr-runtime/antlr
cp antlr/TokenStreamIOException.class antlr-runtime/antlr
cp antlr/TokenStreamRecognitionException.class antlr-runtime/antlr
cp antlr/TokenStream.class antlr-runtime/antlr
cp antlr/Token.class antlr-runtime/antlr
cp antlr/collections/ASTEnumeration.class antlr-runtime/antlr/collections
cp antlr/collections/AST.class antlr-runtime/antlr/collections
cp antlr/collections/impl/ASTArray.class antlr-runtime/antlr/collections
cp antlr/collections/impl/BitSet.class antlr-runtime/antlr/collections
cp antlr/debug/misc/ASTFrame\$1.class antlr-runtime/antlr/debug/misc
cp antlr/debug/misc/ASTFrame\$MyTreeSelectionListener.class antlr-runtime/antlr/debug/misc
cp antlr/debug/misc/ASTFrame.class antlr-runtime/antlr/debug/misc
cp antlr/debug/misc/JTreeASTModel.class antlr-runtime/antlr/debug/misc
cp antlr/debug/misc/JTreeASTPanel.class antlr-runtime/antlr/debug/misc

cd antlr-runtime
jar cvf ../antlr-runtime.jar antlr
cd ..
