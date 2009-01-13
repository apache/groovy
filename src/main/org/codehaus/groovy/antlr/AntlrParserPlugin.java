/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.antlr;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.TokenStreamRecognitionException;
import antlr.collections.AST;
import com.thoughtworks.xstream.XStream;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.codehaus.groovy.antlr.treewalker.*;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.ParserPlugin;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.*;
import org.objectweb.asm.Opcodes;

import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A parser plugin which adapts the JSR Antlr Parser to the Groovy runtime
 *
 * @author <a href="mailto:jstrachan@protique.com">James Strachan</a>
 * @version $Revision$
 */
public class AntlrParserPlugin extends ASTHelper implements ParserPlugin, GroovyTokenTypes {

    protected AST ast;
    private ClassNode classNode;
    private String[] tokenNames;

    public /*final*/ Reduction parseCST(final SourceUnit sourceUnit, Reader reader) throws CompilationFailedException {
        final SourceBuffer sourceBuffer = new SourceBuffer();
        transformCSTIntoAST(sourceUnit, reader, sourceBuffer);
        processAST();
        return outputAST(sourceUnit,sourceBuffer);
    }

    protected void transformCSTIntoAST(SourceUnit sourceUnit, Reader reader, SourceBuffer sourceBuffer) throws CompilationFailedException {
        ast = null;

        setController(sourceUnit);

        // TODO find a way to inject any GroovyLexer/GroovyRecognizer

        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(reader,sourceBuffer);
        GroovyLexer lexer = new GroovyLexer(unicodeReader);
        unicodeReader.setLexer(lexer);
        GroovyRecognizer parser = GroovyRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);
        tokenNames = parser.getTokenNames();
        parser.setFilename(sourceUnit.getName());

        // start parsing at the compilationUnit rule
        try {
            parser.compilationUnit();
        }
        catch (TokenStreamRecognitionException tsre) {
            RecognitionException e = tsre.recog;
            SyntaxException se = new SyntaxException(e.getMessage(),e,e.getLine(),e.getColumn());
            se.setFatal(true);
            sourceUnit.addError(se);
        }
        catch (RecognitionException e) {
            SyntaxException se = new SyntaxException(e.getMessage(),e,e.getLine(),e.getColumn());
            se.setFatal(true);
            sourceUnit.addError(se);
        }
        catch (TokenStreamException e) {
            sourceUnit.addException(e);
        }

        ast = parser.getAST();
    }

    protected void processAST() {
        AntlrASTProcessor snippets = new AntlrASTProcessSnippets();
        ast = snippets.process(ast);
    }
        
    public Reduction outputAST(final SourceUnit sourceUnit, final SourceBuffer sourceBuffer) {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
            	outputASTInVariousFormsIfNeeded(sourceUnit, sourceBuffer);
                return null;
            }
        });
        
        return null; //new Reduction(Tpken.EOF);
    }

    private void outputASTInVariousFormsIfNeeded(SourceUnit sourceUnit, SourceBuffer sourceBuffer) {
        // straight xstream output of AST
        if ("xml".equals(System.getProperty("antlr.ast"))) {
            saveAsXML(sourceUnit.getName(), ast);
        }

        // 'pretty printer' output of AST
        if ("groovy".equals(System.getProperty("antlr.ast"))) {
            try {
                PrintStream out = new PrintStream(new FileOutputStream(sourceUnit.getName() + ".pretty.groovy"));
                Visitor visitor = new SourcePrinter(out,tokenNames);
                AntlrASTProcessor treewalker = new SourceCodeTraversal(visitor);
                treewalker.process(ast);
            } catch (FileNotFoundException e) {
                System.out.println("Cannot create " + sourceUnit.getName() + ".pretty.groovy");
            }
        }

        // output AST in format suitable for opening in http://freemind.sourceforge.net
        // which is a really nice way of seeing the AST, folding nodes etc
        if ("mindmap".equals(System.getProperty("antlr.ast"))) {
            try {
                PrintStream out = new PrintStream(new FileOutputStream(sourceUnit.getName() + ".mm"));
                Visitor visitor = new MindMapPrinter(out,tokenNames);
                AntlrASTProcessor treewalker = new PreOrderTraversal(visitor);
                treewalker.process(ast);
            } catch (FileNotFoundException e) {
                System.out.println("Cannot create " + sourceUnit.getName() + ".mm");
            }
        }

        // include original line/col info and source code on the mindmap output
        if ("extendedMindmap".equals(System.getProperty("antlr.ast"))) {
            try {
                PrintStream out = new PrintStream(new FileOutputStream(sourceUnit.getName() + ".mm"));
                Visitor visitor = new MindMapPrinter(out,tokenNames,sourceBuffer);
                AntlrASTProcessor treewalker = new PreOrderTraversal(visitor);
                treewalker.process(ast);
            } catch (FileNotFoundException e) {
                System.out.println("Cannot create " + sourceUnit.getName() + ".mm");
            }
        }

        // html output of AST
        if ("html".equals(System.getProperty("antlr.ast"))) {
            try {
                PrintStream out = new PrintStream(new FileOutputStream(sourceUnit.getName() + ".html"));
                List v = new ArrayList();
                v.add(new NodeAsHTMLPrinter(out,tokenNames));
                v.add(new SourcePrinter(out,tokenNames));
                Visitor visitors = new CompositeVisitor(v);
                AntlrASTProcessor treewalker = new SourceCodeTraversal(visitors);
                treewalker.process(ast);
            } catch (FileNotFoundException e) {
                System.out.println("Cannot create " + sourceUnit.getName() + ".html");
            }
        }


    }

    private void saveAsXML(String name, AST ast) {
        XStream xstream = new XStream();
        try {
            xstream.toXML(ast, new FileWriter(name + ".antlr.xml"));
            System.out.println("Written AST to " + name + ".antlr.xml");
        }
        catch (Exception e) {
            System.out.println("Couldn't write to " + name + ".antlr.xml");
            e.printStackTrace();
        }
    }

    public ModuleNode buildAST(SourceUnit sourceUnit, ClassLoader classLoader, Reduction cst) throws ParserException {
        setClassLoader(classLoader);
        makeModule();
        try {
            convertGroovy(ast);
        }
        catch (ASTRuntimeException e) {
            throw new ASTParserException(e.getMessage() + ". File: " + sourceUnit.getName(), e);
        }
        return output;
    }

    /**
     * Converts the Antlr AST to the Groovy AST
     */
    protected void convertGroovy(AST node) {
        while (node != null) {
            int type = node.getType();
            switch (type) {
                case PACKAGE_DEF:
                    packageDef(node);
                    break;

                case STATIC_IMPORT:
                case IMPORT:
                    importDef(node);
                    break;

                case CLASS_DEF:
                    classDef(node);
                    break;

                case INTERFACE_DEF:
                    interfaceDef(node);
                    break;

                case METHOD_DEF:
                    methodDef(node);
                    break;
                    
                case ENUM_DEF:
                    enumDef(node);
                    break;

                case ANNOTATION_DEF:
                    annotationDef(node);
                    break;
                    
                default:
                    {
                        Statement statement = statement(node);
                        output.addStatement(statement);
                    }
            }
            node = node.getNextSibling();
        }
    }

    // Top level control structures
    //-------------------------------------------------------------------------

    protected void packageDef(AST packageDef) {
        AST node = packageDef.getFirstChild();
        if (isType(ANNOTATIONS, node)) {
            node = node.getNextSibling();
        }
        String name = qualifiedName(node);
        setPackageName(name);
    }
    
    protected void importDef(AST importNode) {
        boolean isStatic = importNode.getType() == STATIC_IMPORT;

        AST node = importNode.getFirstChild();

        String alias = null;
        if (isType(LITERAL_as, node)) {
            //import is like "import Foo as Bar"
            node = node.getFirstChild();
            AST aliasNode = node.getNextSibling();
            alias = identifier(aliasNode);
        }

        if (node.getNumberOfChildren()==0) {
            String name = identifier(node);
            // import is like  "import Foo"
            ClassNode type = ClassHelper.make(name);
            configureAST(type,importNode);
            importClass(type, name, alias);
            return;
        }

        AST packageNode = node.getFirstChild();
        String packageName = qualifiedName(packageNode);
        AST nameNode = packageNode.getNextSibling();
        if (isType(STAR, nameNode)) {
            if (isStatic) {
                // import is like "import static foo.Bar.*"
                // packageName is actually a className in this case
                ClassNode type = ClassHelper.make(packageName);
                configureAST(type, importNode);
                staticImportClassWithStar(type, packageName);
            } else {
                // import is like "import foo.*"
                importPackageWithStar(packageName);
            }

            if (alias!=null) throw new GroovyBugError(
                    "imports like 'import foo.* as Bar' are not "+
                    "supported and should be caught by the grammar");
        } else {
            String name = identifier(nameNode);
            if (isStatic) {
                // import is like "import static foo.Bar.method"
                // packageName is really class name in this case
                ClassNode type = ClassHelper.make(packageName);
                configureAST(type, importNode);
                staticImportMethodOrField(type, name, alias);
            } else {
                // import is like "import foo.Bar"
                ClassNode type = ClassHelper.make(packageName+"."+name);
                configureAST(type, importNode);
                importClass(type, name, alias);
            }
        }
    }

    protected void annotationDef(AST classDef) {
        List annotations = new ArrayList();
        AST node = classDef.getFirstChild();
        int modifiers = Opcodes.ACC_PUBLIC;
        if (isType(MODIFIERS, node)) {
            modifiers = modifiers(node, annotations, modifiers);
            checkNoInvalidModifier(classDef, "Annotation Definition", modifiers, Opcodes.ACC_SYNCHRONIZED, "synchronized");
            node = node.getNextSibling();
        }
        modifiers |= Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE | Opcodes.ACC_ANNOTATION;

        String name = identifier(node);
        node = node.getNextSibling();
        ClassNode superClass = ClassHelper.OBJECT_TYPE;

        GenericsType[] genericsType = null;
        if (isType(TYPE_PARAMETERS,node)) {
            genericsType = makeGenericsType(node);
            node = node.getNextSibling();
        }
        
        ClassNode[] interfaces = ClassNode.EMPTY_ARRAY;
        if (isType(EXTENDS_CLAUSE, node)) {
            interfaces = interfaces(node);
            node = node.getNextSibling();
        }

        addNewClassName(name);
        classNode = new ClassNode(dot(getPackageName(), name), modifiers, superClass, interfaces, null);
        classNode.addAnnotations(annotations);
        classNode.setGenericsTypes(genericsType);
        classNode.addInterface(ClassHelper.Annotation_TYPE);
        configureAST(classNode, classDef);

        assertNodeType(OBJBLOCK, node);
        objectBlock(node);
        output.addClass(classNode);
        classNode = null;
    }
    
    protected void interfaceDef(AST classDef) {
        List annotations = new ArrayList();
        AST node = classDef.getFirstChild();
        int modifiers = Opcodes.ACC_PUBLIC;
        if (isType(MODIFIERS, node)) {
            modifiers = modifiers(node, annotations, modifiers);
            checkNoInvalidModifier(classDef, "Interface", modifiers, Opcodes.ACC_SYNCHRONIZED, "synchronized");
            node = node.getNextSibling();
        }
        modifiers |= Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE;

        String name = identifier(node);
        node = node.getNextSibling();
        ClassNode superClass = ClassHelper.OBJECT_TYPE;

        GenericsType[] genericsType = null;
        if (isType(TYPE_PARAMETERS,node)) {
            genericsType = makeGenericsType(node);
            node = node.getNextSibling();
        }
        
        ClassNode[] interfaces = ClassNode.EMPTY_ARRAY;
        if (isType(EXTENDS_CLAUSE, node)) {
            interfaces = interfaces(node);
            node = node.getNextSibling();
        }

        addNewClassName(name);
        classNode = new ClassNode(dot(getPackageName(), name), modifiers, superClass, interfaces, null);
        classNode.addAnnotations(annotations);
        classNode.setGenericsTypes(genericsType);
        configureAST(classNode, classDef);

        assertNodeType(OBJBLOCK, node);
        objectBlock(node);
        output.addClass(classNode);
        classNode = null;
    }

    protected void classDef(AST classDef) {
        List annotations = new ArrayList();
        AST node = classDef.getFirstChild();
        int modifiers = Opcodes.ACC_PUBLIC;
        if (isType(MODIFIERS, node)) {
            modifiers = modifiers(node, annotations, modifiers);
            checkNoInvalidModifier(classDef, "Class", modifiers, Opcodes.ACC_SYNCHRONIZED, "synchronized");
            node = node.getNextSibling();
        }

        String name = identifier(node);
        node = node.getNextSibling();
        
        GenericsType[] genericsType = null;
        if (isType(TYPE_PARAMETERS,node)) {
            genericsType = makeGenericsType(node);
            node = node.getNextSibling();
        }

        ClassNode superClass = null;
        if (isType(EXTENDS_CLAUSE, node)) {
            superClass = makeTypeWithArguments(node);
            node = node.getNextSibling();
        }

        ClassNode[] interfaces = ClassNode.EMPTY_ARRAY;
        if (isType(IMPLEMENTS_CLAUSE, node)) {
            interfaces = interfaces(node);
            node = node.getNextSibling();
        }

        // TODO read mixins
        MixinNode[] mixins = {};

        addNewClassName(name);
        classNode = new ClassNode(dot(getPackageName(), name), modifiers, superClass, interfaces, mixins);
        classNode.addAnnotations(annotations);
        classNode.setGenericsTypes(genericsType);
        configureAST(classNode, classDef);

        assertNodeType(OBJBLOCK, node);
        objectBlock(node);
        output.addClass(classNode);
        classNode = null;
    }

    protected void objectBlock(AST objectBlock) {
        for (AST node = objectBlock.getFirstChild(); node != null; node = node.getNextSibling()) {
            int type = node.getType();
            switch (type) {
                case OBJBLOCK:
                    objectBlock(node);
                    break;

                case ANNOTATION_FIELD_DEF:
                case METHOD_DEF:
                    methodDef(node);
                    break;

                case CTOR_IDENT:
                    constructorDef(node);
                    break;

                case VARIABLE_DEF:
                    fieldDef(node);
                    break;

                case STATIC_INIT:
                    staticInit(node);
                    break;
                    
                case INSTANCE_INIT:
                    objectInit(node);
                    break;
                    
                case ENUM_DEF:
                    enumDef(node);
                    break;      
                    
                case ENUM_CONSTANT_DEF:
                    enumConstantDef(node);
                    break;
                    
                default:
                    unknownAST(node);
            }
        }
    }
    
    protected void enumDef(AST enumNode) {
        assertNodeType(ENUM_DEF, enumNode);
        List annotations = new ArrayList();
        
        AST node = enumNode.getFirstChild();
        int modifiers = Opcodes.ACC_PUBLIC;
        if (isType(MODIFIERS, node)) {
            modifiers = modifiers(node,annotations,modifiers);
            node = node.getNextSibling();
        }
        
        String name = identifier(node);
        node = node.getNextSibling();
       
        ClassNode[] interfaces = interfaces(node);
        node = node.getNextSibling();
        
        ClassNode enumClass = EnumHelper.makeEnumNode(dot(getPackageName(),name),modifiers,interfaces,classNode);
        ClassNode oldNode = classNode;
        classNode = enumClass;
        assertNodeType(OBJBLOCK, node);
        objectBlock(node);
        classNode = oldNode;
        
        output.addClass(enumClass);
    }
    
    protected void enumConstantDef(AST node) {
        assertNodeType(ENUM_CONSTANT_DEF, node);
        AST element = node.getFirstChild();
        if (isType(ANNOTATIONS,element)) {
            element = element.getNextSibling();
        }
        String identifier = identifier(element);
        Expression init = null;
        element = element.getNextSibling();
        if (element!=null) {
            init = expression(element);
            if (isType(ELIST,element)) {
            	if(init instanceof ListExpression && !((ListExpression)init).isWrapped()) {
                    ListExpression le = new ListExpression();
                    le.addExpression(init);
                    init = le;
            	}
            }
        }
        EnumHelper.addEnumConstant(classNode, identifier, init);
    }
    
    protected void throwsList(AST node,List list) {
    	String name;
    	if (isType(DOT, node)) {
    		name = qualifiedName(node);
    	} else {
    		name = identifier(node);
    	}
    	ClassNode exception = ClassHelper.make(name);
    	configureAST(exception, node);
    	list.add(exception);
    	AST next = node.getNextSibling();
    	if (next!=null) throwsList(next, list);
    }

	protected void methodDef(AST methodDef) {
        List annotations = new ArrayList();
        AST node = methodDef.getFirstChild();
        
        GenericsType[] generics=null;
        if (isType(TYPE_PARAMETERS, node)) {
            generics = makeGenericsType(node);
            node = node.getNextSibling();
        }
        
        int modifiers = Opcodes.ACC_PUBLIC;
        if (isType(MODIFIERS, node)) {
            modifiers = modifiers(node, annotations, modifiers);
            checkNoInvalidModifier(methodDef, "Method", modifiers, Opcodes.ACC_VOLATILE, "volatile");
            node = node.getNextSibling();
        }

        if (isAnInterface()) {
            modifiers |= Opcodes.ACC_ABSTRACT;
        }

        ClassNode returnType = null;
        if (isType(TYPE, node)) {
            returnType = makeTypeWithArguments(node);
            node = node.getNextSibling();
        }

        String name = identifier(node);
        if (classNode != null && !classNode.isAnnotationDefinition()) {
            if (classNode.getNameWithoutPackage().equals(name)) {
                if (isAnInterface()) {
                    throw new ASTRuntimeException(methodDef, "Constructor not permitted within an interface.");
                }
                throw new ASTRuntimeException(methodDef, "Invalid constructor format. Remove '" + returnType.getName() +
                        "' as the return type if you want a constructor, or use a different name if you want a method.");
            }
        }
        node = node.getNextSibling();
        
        Parameter[] parameters = Parameter.EMPTY_ARRAY;
        ClassNode[] exceptions= ClassNode.EMPTY_ARRAY;
        
        if (classNode==null || !classNode.isAnnotationDefinition()) {
            
            assertNodeType(PARAMETERS, node);
            parameters = parameters(node);
            if (parameters==null) parameters = Parameter.EMPTY_ARRAY;
            node = node.getNextSibling();
            
            if (isType(LITERAL_throws, node)) {
            	AST throwsNode = node.getFirstChild();
            	List exceptionList = new ArrayList();
            	throwsList(throwsNode, exceptionList);
            	exceptions = (ClassNode[]) exceptionList.toArray(exceptions);
            	node = node.getNextSibling();
            }
        }

        boolean hasAnnotationDefault = false;
        Statement code = null;
        if ((modifiers & Opcodes.ACC_ABSTRACT) == 0) {
            if (node==null) {
                throw new ASTRuntimeException(methodDef, "You defined a method without body. Try adding a body, or declare it abstract.");
            }
            assertNodeType(SLIST, node);
            code = statementList(node);
        }  else if (node!=null && classNode.isAnnotationDefinition()) {
            code = statement(node);
            hasAnnotationDefault = true;
        }

        MethodNode methodNode = new MethodNode(name, modifiers, returnType, parameters, exceptions, code);
        methodNode.addAnnotations(annotations);
        methodNode.setGenericsTypes(generics);
        methodNode.setAnnotationDefault(hasAnnotationDefault);
        configureAST(methodNode, methodDef);

        if (classNode != null) {
            classNode.addMethod(methodNode);
        }
        else {
            output.addMethod(methodNode);
        }
    }

    private void checkNoInvalidModifier(AST node, String nodeType, int modifiers, int modifier, String modifierText) {
        if ((modifiers & modifier) != 0) {
            throw new ASTRuntimeException(node, nodeType + " has an incorrect modifier '" + modifierText + "'.");
        }
    }

    private boolean isAnInterface() {
        return classNode != null && (classNode.getModifiers() & Opcodes.ACC_INTERFACE) > 0;
    }

    protected void staticInit(AST staticInit) {        
        BlockStatement code = (BlockStatement) statementList(staticInit);
        classNode.addStaticInitializerStatements(code.getStatements(),false);
    }
    
    protected void objectInit(AST init) {        
        BlockStatement code = (BlockStatement) statementList(init);
        classNode.addObjectInitializerStatements(code);
    }
    
    protected void constructorDef(AST constructorDef) {
        List annotations = new ArrayList();
        AST node = constructorDef.getFirstChild();
        int modifiers = Opcodes.ACC_PUBLIC;
        if (isType(MODIFIERS, node)) {
            modifiers = modifiers(node, annotations, modifiers);
            node = node.getNextSibling();
        }

        assertNodeType(PARAMETERS, node);
        Parameter[] parameters = parameters(node);
        if (parameters == null) parameters = Parameter.EMPTY_ARRAY;
        node = node.getNextSibling();

        ClassNode[] exceptions= ClassNode.EMPTY_ARRAY;
        if (isType(LITERAL_throws, node)) {
        	AST throwsNode = node.getFirstChild();
        	List exceptionList = new ArrayList();
        	throwsList(throwsNode, exceptionList);
        	exceptions = (ClassNode[]) exceptionList.toArray(exceptions);
        	node = node.getNextSibling();
        }
        
        assertNodeType(SLIST, node);
        Statement code = statementList(node);

        ConstructorNode constructorNode = classNode.addConstructor(modifiers, parameters, exceptions, code);
        constructorNode.addAnnotations(annotations);
        configureAST(constructorNode, constructorDef);
    }

    protected void fieldDef(AST fieldDef) {
        List annotations = new ArrayList();
        AST node = fieldDef.getFirstChild();

        int modifiers = 0;
        if (isType(MODIFIERS, node)) {
            modifiers = modifiers(node, annotations, modifiers);
            node = node.getNextSibling();
        }

        if (classNode.isInterface()) {
        	modifiers |= Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;
        	if ( (modifiers & (Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) == 0) {
        		modifiers |= Opcodes.ACC_PUBLIC;
        	}
        }

        ClassNode type = null;
        if (isType(TYPE, node)) {
            type = makeTypeWithArguments(node);
            node = node.getNextSibling();
        }

        String name = identifier(node);
        node = node.getNextSibling();

        Expression initialValue = null;
        if (node != null) {
            assertNodeType(ASSIGN, node);
            initialValue = expression(node.getFirstChild());
        }

        if (classNode.isInterface() && initialValue == null && type != null) {
            if (type==ClassHelper.int_TYPE) {
                initialValue = new ConstantExpression(Integer.valueOf(0));
            }
            else if (type==ClassHelper.long_TYPE) {
                initialValue = new ConstantExpression(new Long(0L));
            }
            else if (type==ClassHelper.double_TYPE) {
                initialValue = new ConstantExpression(new Double(0.0));
            }
            else if (type==ClassHelper.float_TYPE) {
                initialValue = new ConstantExpression(new Float(0.0F));
            }
            else if (type==ClassHelper.boolean_TYPE) {
                initialValue = ConstantExpression.FALSE;
            }
            else if (type==ClassHelper.short_TYPE) {
                initialValue = new ConstantExpression(new Short((short) 0));
            }
            else if (type==ClassHelper.byte_TYPE) {
                initialValue = new ConstantExpression(new Byte((byte) 0));
            }
            else if (type==ClassHelper.char_TYPE) {
                initialValue = new ConstantExpression(new Character((char) 0));
            }
        }


        FieldNode fieldNode = new FieldNode(name, modifiers, type, classNode, initialValue);
        fieldNode.addAnnotations(annotations);
        configureAST(fieldNode, fieldDef);

        if (!hasVisibility(modifiers)) {
            // let's set the modifiers on the field
            int fieldModifiers = 0;
            int flags = Opcodes.ACC_STATIC | Opcodes.ACC_TRANSIENT | Opcodes.ACC_VOLATILE | Opcodes.ACC_FINAL;

            if (!hasVisibility(modifiers)) {
                modifiers |= Opcodes.ACC_PUBLIC;
                fieldModifiers |= Opcodes.ACC_PRIVATE;
            }

            // let's pass along any other modifiers we need
            fieldModifiers |= (modifiers & flags);
            fieldNode.setModifiers(fieldModifiers);
            fieldNode.setSynthetic(true);
            
            // in the case that there is already a field, we would
            // like to use that field, instead of the default field
            // for the property
            FieldNode storedNode = classNode.getDeclaredField(fieldNode.getName());
            if (storedNode!=null && !classNode.hasProperty(name)) {
            	fieldNode = storedNode;
            	// we remove it here, because addProperty will add it
            	// again and we want to avoid it showing up multiple 
            	// times in the fields list.
        		classNode.getFields().remove(storedNode);
            }
            
            PropertyNode propertyNode = new PropertyNode(fieldNode, modifiers, null, null);
            configureAST(propertyNode, fieldDef);
            classNode.addProperty(propertyNode);
        } else {
        	fieldNode.setModifiers(modifiers);
        	// if there is a property of that name, then a field of that
        	// name already exists, which means this new field here should
        	// be used instead of the field the property originally has.
        	PropertyNode pn = classNode.getProperty(name);
        	if (pn!=null && pn.getField().isSynthetic()) {
        		classNode.getFields().remove(pn.getField());
        		pn.setField(fieldNode);
        	}
            classNode.addField(fieldNode);
        }
    }

    protected ClassNode[] interfaces(AST node) {
        List interfaceList = new ArrayList();
        for (AST implementNode = node.getFirstChild(); implementNode != null; implementNode = implementNode.getNextSibling()) {
            interfaceList.add(makeTypeWithArguments(implementNode));
        }
        ClassNode[] interfaces = ClassNode.EMPTY_ARRAY;
        if (!interfaceList.isEmpty()) {
            interfaces = new ClassNode[interfaceList.size()];
            interfaceList.toArray(interfaces);

        }
        return interfaces;
    }

    protected Parameter[] parameters(AST parametersNode) {
        AST node = parametersNode.getFirstChild();
        if (node == null) {
        	if (isType(IMPLICIT_PARAMETERS, parametersNode)) return Parameter.EMPTY_ARRAY;
            return null;
        }
        else {
            List parameters = new ArrayList();
            do {
                parameters.add(parameter(node));
                node = node.getNextSibling();
            }
            while (node != null);
            Parameter[] answer = new Parameter[parameters.size()];
            parameters.toArray(answer);
            return answer;
        }
    }

    protected Parameter parameter(AST paramNode) {
        List annotations = new ArrayList();
        boolean variableParameterDef = isType(VARIABLE_PARAMETER_DEF,paramNode);
        AST node = paramNode.getFirstChild();

        int modifiers = 0;
        if (isType(MODIFIERS, node)) {
            modifiers = modifiers(node, annotations, modifiers);
            node = node.getNextSibling();
        }

        ClassNode type = ClassHelper.DYNAMIC_TYPE;
        if (isType(TYPE, node)) {
            type = makeTypeWithArguments(node);
            if (variableParameterDef) type = type.makeArray();
            node = node.getNextSibling();
        }

        String name = identifier(node);
        node = node.getNextSibling();

        VariableExpression leftExpression = new VariableExpression(name, type);
        configureAST(leftExpression, paramNode);

        Parameter parameter = null;
        if (node != null) {
            assertNodeType(ASSIGN, node);
            Expression rightExpression = expression(node.getFirstChild());
            if (isAnInterface()) {
                throw new ASTRuntimeException(node, "Cannot specify default value for method parameter '" + name + " = " + rightExpression.getText() + "' inside an interface");
            }
            parameter = new Parameter(type, name, rightExpression);
        }
        else
            parameter = new Parameter(type, name);

        configureAST(parameter, paramNode);
        parameter.addAnnotations(annotations);
        return parameter;
    }

    protected int modifiers(AST modifierNode, List annotations, int defaultModifiers) {
        assertNodeType(MODIFIERS, modifierNode);

        boolean access = false;
        int answer = 0;

        for (AST node = modifierNode.getFirstChild(); node != null; node = node.getNextSibling()) {
            int type = node.getType();
            switch (type) {
                case STATIC_IMPORT:
                    // ignore
                    break;

                    // annotations
                case ANNOTATION:
                    annotations.add(annotation(node));
                    break;

                    // core access scope modifiers
                case LITERAL_private:
                    answer = setModifierBit(node, answer, Opcodes.ACC_PRIVATE);
                    access = setAccessTrue(node, access);
                    break;

                case LITERAL_protected:
                    answer = setModifierBit(node, answer, Opcodes.ACC_PROTECTED);
                    access = setAccessTrue(node, access);
                    break;

                case LITERAL_public:
                    answer = setModifierBit(node, answer, Opcodes.ACC_PUBLIC);
                    access = setAccessTrue(node, access);
                    break;

                    // other modifiers
                case ABSTRACT:
                    answer = setModifierBit(node, answer, Opcodes.ACC_ABSTRACT);
                    break;

                case FINAL:
                    answer = setModifierBit(node, answer, Opcodes.ACC_FINAL);
                    break;

                case LITERAL_native:
                    answer = setModifierBit(node, answer, Opcodes.ACC_NATIVE);
                    break;

                case LITERAL_static:
                    answer = setModifierBit(node, answer, Opcodes.ACC_STATIC);
                    break;

                case STRICTFP:
                    answer = setModifierBit(node, answer, Opcodes.ACC_STRICT);
                    break;

                case LITERAL_synchronized:
                    answer = setModifierBit(node, answer, Opcodes.ACC_SYNCHRONIZED);
                    break;

                case LITERAL_transient:
                    answer = setModifierBit(node, answer, Opcodes.ACC_TRANSIENT);
                    break;

                case LITERAL_volatile:
                    answer = setModifierBit(node, answer, Opcodes.ACC_VOLATILE);
                    break;

                default:
                    unknownAST(node);
            }
        }
        if (!access) {
            answer |= defaultModifiers;
        }
        return answer;
    }

    protected boolean setAccessTrue(AST node, boolean access) {
        if (!access) {
            return true;
        }
        else {
            throw new ASTRuntimeException(node, "Cannot specify modifier: " + node.getText() + " when access scope has already been defined");
        }
    }

    protected int setModifierBit(AST node, int answer, int bit) {
        if ((answer & bit) != 0) {
            throw new ASTRuntimeException(node, "Cannot repeat modifier: " + node.getText());
        }
        return answer | bit;
    }

    protected AnnotationNode annotation(AST annotationNode) {
        AST node = annotationNode.getFirstChild();
        String name = qualifiedName(node);
        AnnotationNode annotatedNode = new AnnotationNode(ClassHelper.make(name));
        configureAST(annotatedNode, node);
        while (true) {
            node = node.getNextSibling();
            if (isType(ANNOTATION_MEMBER_VALUE_PAIR, node)) {
                AST memberNode = node.getFirstChild();
                String param = identifier(memberNode);
                Expression expression = expression(memberNode.getNextSibling());
                annotatedNode.addMember(param, expression);
            } else {
                break;
            }
        }
        return annotatedNode;
    }



    // Statements
    //-------------------------------------------------------------------------

    protected Statement statement(AST node) {
        Statement statement = null;
        int type = node.getType();
        switch (type) {
            case SLIST:
            case LITERAL_finally:
                statement = statementList(node);
                break;

            case METHOD_CALL:
                statement = methodCall(node);
                break;

            case VARIABLE_DEF:
                statement = variableDef(node);
                break;


            case LABELED_STAT:
                statement = labelledStatement(node);
                break;

            case LITERAL_assert:
                statement = assertStatement(node);
                break;

            case LITERAL_break:
                statement = breakStatement(node);
                break;

            case LITERAL_continue:
                statement = continueStatement(node);
                break;

            case LITERAL_if:
                statement = ifStatement(node);
                break;

            case LITERAL_for:
                statement = forStatement(node);
                break;

            case LITERAL_return:
                statement = returnStatement(node);
                break;

            case LITERAL_synchronized:
                statement = synchronizedStatement(node);
                break;

            case LITERAL_switch:
                statement = switchStatement(node);
                break;

            case LITERAL_try:
                statement = tryStatement(node);
                break;

            case LITERAL_throw:
                statement = throwStatement(node);
                break;

            case LITERAL_while:
                statement = whileStatement(node);
                break;

            default:
                statement = new ExpressionStatement(expression(node));
        }
        if (statement != null) {
            configureAST(statement, node);
        }
        return statement;
    }

    protected Statement statementList(AST code) {
        Statement st = statementListNoChild(code.getFirstChild(),code);
        return st;
    }

    protected Statement statementListNoChild(AST node, AST alternativeConfigureNode) {
        BlockStatement block = new BlockStatement();
        // alternativeConfigureNode is used only to set the source position
        if (node!=null) {
            configureAST(block, node);
        } else {
            configureAST(block, alternativeConfigureNode);
        }
        for (; node != null; node = node.getNextSibling()) {
            block.addStatement(statement(node));
        }
        return block;
    }

    protected Statement assertStatement(AST assertNode) {
        AST node = assertNode.getFirstChild();
        BooleanExpression booleanExpression = booleanExpression(node);
        Expression messageExpression = null;

        node = node.getNextSibling();
        if (node != null) {
            messageExpression = expression(node);
        }
        else {
            messageExpression = ConstantExpression.NULL;
        }
        AssertStatement assertStatement = new AssertStatement(booleanExpression, messageExpression);
        configureAST(assertStatement, assertNode);
        return assertStatement;
    }

    protected Statement breakStatement(AST node) {
        BreakStatement breakStatement = new BreakStatement(label(node));
        configureAST(breakStatement, node);
        return breakStatement;
    }

    protected Statement continueStatement(AST node) {
        ContinueStatement continueStatement = new ContinueStatement(label(node));
        configureAST(continueStatement, node);
        return continueStatement;
    }

    protected Statement forStatement(AST forNode) {
        AST inNode = forNode.getFirstChild();
        Expression collectionExpression;
        Parameter forParameter;
        if (isType(CLOSURE_LIST, inNode)) {
            ClosureListExpression clist = closureListExpression(inNode);
            int size = clist.getExpressions().size();
            if (size != 3) {
                throw new ASTRuntimeException(inNode, "3 expressions are required for the classic for loop, you gave " + size);
            }
            collectionExpression = clist;
            forParameter = ForStatement.FOR_LOOP_DUMMY;
        } else {
            AST variableNode = inNode.getFirstChild();
            AST collectionNode = variableNode.getNextSibling();

            ClassNode type = ClassHelper.OBJECT_TYPE;
            if (isType(VARIABLE_DEF, variableNode)) {
                AST node = variableNode.getFirstChild();
                // skip the final modifier if it's present
                if (isType(MODIFIERS, node)) {
                    int modifiersMask = modifiers(node, new ArrayList(), 0);
                    // only final modifier allowed
                    if ((modifiersMask & ~Opcodes.ACC_FINAL) != 0) {
                        throw new ASTRuntimeException(node, "Only the 'final' modifier is allowed in front of the for loop variable.");
                    }
                    node = node.getNextSibling();
                }
                type = makeTypeWithArguments(node);

                variableNode = node.getNextSibling();
            }
            String variable = identifier(variableNode);

            collectionExpression = expression(collectionNode);
            forParameter = new Parameter(type, variable);
            configureAST(forParameter, variableNode);
        }

        final AST node = inNode.getNextSibling();
        Statement block;
        if (isType(SEMI, node)) {
            block = EmptyStatement.INSTANCE;
        } else {
            block = statement(node);
        }
        ForStatement forStatement = new ForStatement(forParameter, collectionExpression, block);
        configureAST(forStatement, forNode);
        return forStatement;
    }

    protected Statement ifStatement(AST ifNode) {
        AST node = ifNode.getFirstChild();
        assertNodeType(EXPR, node);
        BooleanExpression booleanExpression = booleanExpression(node);

        node = node.getNextSibling();
        Statement ifBlock = statement(node);

        Statement elseBlock = EmptyStatement.INSTANCE;
        node = node.getNextSibling();
        if (node != null) {
            elseBlock = statement(node);
        }
        IfStatement ifStatement = new IfStatement(booleanExpression, ifBlock, elseBlock);
        configureAST(ifStatement, ifNode);
        return ifStatement;
    }

    protected Statement labelledStatement(AST labelNode) {
        AST node = labelNode.getFirstChild();
        String label = identifier(node);
        Statement statement = statement(node.getNextSibling());
        statement.setStatementLabel(label);
        return statement;
    }

    protected Statement methodCall(AST code) {
        Expression expression = methodCallExpression(code);
        ExpressionStatement expressionStatement = new ExpressionStatement(expression);
        configureAST(expressionStatement, code);
        return expressionStatement;
    }

    protected Expression declarationExpression(AST variableDef) {
        AST node = variableDef.getFirstChild();
        ClassNode type = null;
        List annotations = new ArrayList();
        if (isType(MODIFIERS, node)) {
            // force check of modifier conflicts
            modifiers(node, annotations, 0);
            node = node.getNextSibling();
        }
        if (isType(TYPE, node)) {
            type = makeTypeWithArguments(node);
            node = node.getNextSibling();
        }
        
        Expression leftExpression;
        Expression rightExpression = ConstantExpression.NULL;
        AST right;
        
        if (isType(ASSIGN, node)) {
            node = node.getFirstChild();
            AST left = node.getFirstChild();
            ArgumentListExpression alist = new ArgumentListExpression();
            for (AST varDef = left; varDef!=null; varDef=varDef.getNextSibling()) {
                assertNodeType(VARIABLE_DEF, varDef);
                DeclarationExpression de = (DeclarationExpression) declarationExpression(varDef);
                alist.addExpression(de.getVariableExpression());
            }
            leftExpression = alist;
            right = node.getNextSibling();
            if (right != null) rightExpression = expression(right);
        } else {
            String name = identifier(node);
            leftExpression = new VariableExpression(name, type);
            right = node.getNextSibling();
            if (right != null) {
                assertNodeType(ASSIGN, right);
                rightExpression = expression(right.getFirstChild());
            }
        }
        
        configureAST(leftExpression, node);
        
        Token token = makeToken(Types.ASSIGN, variableDef);
        DeclarationExpression expression = new DeclarationExpression(leftExpression, token, rightExpression);
        configureAST(expression, variableDef);
        ExpressionStatement expressionStatement = new ExpressionStatement(expression);
        configureAST(expressionStatement, variableDef);
        return expression;
    }
    
    protected Statement variableDef(AST variableDef) {
        ExpressionStatement expressionStatement = new ExpressionStatement(declarationExpression(variableDef));
        configureAST(expressionStatement, variableDef);
        return expressionStatement;
    }

    protected Statement returnStatement(AST node) {
        AST exprNode = node.getFirstChild();

        // This will pick up incorrect sibling node if 'node' is a plain 'return'
		//
		//if (exprNode == null) {
        //    exprNode = node.getNextSibling();
        //}
        if (exprNode != null) {
            Expression expression = expression(exprNode);
            if (expression instanceof ConstantExpression) {
                ConstantExpression constantExpr = (ConstantExpression) expression;
                if (constantExpr.getValue() == null) {
                    return ReturnStatement.RETURN_NULL_OR_VOID;
                }
            }
            ReturnStatement returnStatement = new ReturnStatement(expression);
            configureAST(returnStatement, node);
            return returnStatement;
        }
        else {
            return ReturnStatement.RETURN_NULL_OR_VOID;
        }
    }

    protected Statement switchStatement(AST switchNode) {
        AST node = switchNode.getFirstChild();
        Expression expression = expression(node);
        Statement defaultStatement = EmptyStatement.INSTANCE;

        List list = new ArrayList();
        for (node = node.getNextSibling(); isType(CASE_GROUP, node); node = node.getNextSibling()) {
            AST child = node.getFirstChild();
            if (isType(LITERAL_case, child)) {
                List cases = new LinkedList();
                // default statement can be grouped with previous case
                defaultStatement = caseStatements(child, cases);
                list.addAll(cases);
            } else {
                defaultStatement = statement(child.getNextSibling());
            }
        }
        if (node != null) {
            unknownAST(node);
        }
        SwitchStatement switchStatement = new SwitchStatement(expression, list, defaultStatement);
        configureAST(switchStatement, switchNode);
        return switchStatement;
    }

    protected Statement caseStatements(AST node, List cases) {
        List expressions = new LinkedList();
        Statement statement = EmptyStatement.INSTANCE;
        Statement defaultStatement = EmptyStatement.INSTANCE;
        AST nextSibling = node;
        do {
            Expression expression = expression(nextSibling.getFirstChild());
            expressions.add(expression);
            nextSibling = nextSibling.getNextSibling();
        } while (isType(LITERAL_case, nextSibling));
        if (nextSibling != null) {
            if (isType(LITERAL_default, nextSibling)) {
                defaultStatement = statement(nextSibling.getNextSibling());
                statement = EmptyStatement.INSTANCE;
            } else {
                statement = statement(nextSibling);
            }
        }
        for (Iterator iterator = expressions.iterator(); iterator.hasNext();) {
            Expression expr = (Expression) iterator.next();
            Statement stmt;
            if (iterator.hasNext()) {
                stmt = new CaseStatement(expr,EmptyStatement.INSTANCE);
            } else {
                stmt = new CaseStatement(expr,statement);
            }
            configureAST(stmt,node);
            cases.add(stmt);
        }
        return defaultStatement;
    }

    protected Statement synchronizedStatement(AST syncNode) {
        AST node = syncNode.getFirstChild();
        Expression expression = expression(node);
        Statement code = statement(node.getNextSibling());
        SynchronizedStatement synchronizedStatement = new SynchronizedStatement(expression, code);
        configureAST(synchronizedStatement, syncNode);
        return synchronizedStatement;
    }

    protected Statement throwStatement(AST node) {
        AST expressionNode = node.getFirstChild();
        if (expressionNode == null) {
            expressionNode = node.getNextSibling();
        }
        if (expressionNode == null) {
            throw new ASTRuntimeException(node, "No expression available");
        }
        ThrowStatement throwStatement = new ThrowStatement(expression(expressionNode));
        configureAST(throwStatement, node);
        return throwStatement;
    }

    protected Statement tryStatement(AST tryStatementNode) {
        AST tryNode = tryStatementNode.getFirstChild();
        Statement tryStatement = statement(tryNode);
        Statement finallyStatement = EmptyStatement.INSTANCE;
        AST node = tryNode.getNextSibling();

        // let's do the catch nodes
        List catches = new ArrayList();
        for (; node != null && isType(LITERAL_catch, node); node = node.getNextSibling()) {
            catches.add(catchStatement(node));
        }

        if (isType(LITERAL_finally, node)) {
            finallyStatement = statement(node);
            node = node.getNextSibling();
        }

        TryCatchStatement tryCatchStatement = new TryCatchStatement(tryStatement, finallyStatement);
        configureAST(tryCatchStatement, tryStatementNode);
        for (Iterator iter = catches.iterator(); iter.hasNext();) {
            CatchStatement statement = (CatchStatement) iter.next();
            tryCatchStatement.addCatch(statement);
        }
        return tryCatchStatement;
    }

    protected CatchStatement catchStatement(AST catchNode) {
        AST node = catchNode.getFirstChild();
        Parameter parameter = parameter(node);
        ClassNode exceptionType = parameter.getType();
        String variable = parameter.getName();
        node = node.getNextSibling();
        Statement code = statement(node);
        Parameter catchParameter = new Parameter(exceptionType,variable);
        CatchStatement answer = new CatchStatement(catchParameter, code);
        configureAST(answer, catchNode);
        return answer;
    }

    protected Statement whileStatement(AST whileNode) {
        AST node = whileNode.getFirstChild();
        assertNodeType(EXPR, node);
        // TODO remove this once we support declarations in the while condition
        if (isType(VARIABLE_DEF, node.getFirstChild())) {
            throw new ASTRuntimeException(whileNode,
                    "While loop condition contains a declaration; this is currently unsupported.");
        }
        BooleanExpression booleanExpression = booleanExpression(node);

        node = node.getNextSibling();
        Statement block;
        if (isType(SEMI, node)) {
            block = EmptyStatement.INSTANCE;
        } else {
            block = statement(node);
        }
        WhileStatement whileStatement = new WhileStatement(booleanExpression, block);
        configureAST(whileStatement, whileNode);
        return whileStatement;
    }



    // Expressions
    //-------------------------------------------------------------------------

    protected Expression expression(AST node) {
        return expression(node,false);
    }
    
    protected Expression expression(AST node, boolean convertToConstant) {
        Expression expression = expressionSwitch(node);
        if (convertToConstant && expression instanceof VariableExpression) {
            // a method name can never be a VariableExprssion, so it must converted
            // to a ConstantExpression then. This is needed as the expression
            // method doesn't know we want a ConstantExpression instead of a
            // VariableExpression
            VariableExpression ve = (VariableExpression) expression;
            if (!ve.isThisExpression() && !ve.isSuperExpression()) {
                expression = new ConstantExpression(ve.getName());
            }
        }
        configureAST(expression, node);
        return expression;       
    }

    protected Expression expressionSwitch(AST node) {
        int type = node.getType();
        switch (type) {
            case EXPR:
                return expression(node.getFirstChild());

            case ELIST:
                return expressionList(node);

            case SLIST:
                return blockExpression(node);

            case CLOSABLE_BLOCK:
                return closureExpression(node);

            case SUPER_CTOR_CALL:
                return specialConstructorCallExpression(node,ClassNode.SUPER);

            case METHOD_CALL:
                return methodCallExpression(node);

            case LITERAL_new:
                return constructorCallExpression(node);

            case CTOR_CALL:
                return specialConstructorCallExpression(node,ClassNode.THIS);

            case QUESTION: 
            case ELVIS_OPERATOR:
                return ternaryExpression(node);

            case OPTIONAL_DOT:
            case SPREAD_DOT:
            case DOT:
                return dotExpression(node);

            case IDENT:
            case LITERAL_boolean:
            case LITERAL_byte:
            case LITERAL_char:
            case LITERAL_double:
            case LITERAL_float:
            case LITERAL_int:
            case LITERAL_long:
            case LITERAL_short:
            case LITERAL_void:
            case LITERAL_this:
            case LITERAL_super:
                return variableExpression(node);

            case LIST_CONSTRUCTOR:
                return listExpression(node);

            case MAP_CONSTRUCTOR:
                return mapExpression(node);

            case LABELED_ARG:
                return mapEntryExpression(node);

            case SPREAD_ARG:
                return spreadExpression(node);

            case SPREAD_MAP_ARG:
                return spreadMapExpression(node);

            // commented out of groovy.g due to non determinisms
            //case MEMBER_POINTER_DEFAULT:
            //    return defaultMethodPointerExpression(node);

            case MEMBER_POINTER:
                return methodPointerExpression(node);

            case INDEX_OP:
                return indexExpression(node);

            case LITERAL_instanceof:
                return instanceofExpression(node);

            case LITERAL_as:
                return asExpression(node);

            case TYPECAST:
                return castExpression(node);

                // literals

            case LITERAL_true:
                return literalExpression(node, Boolean.TRUE);
            case LITERAL_false:
                return literalExpression(node, Boolean.FALSE);
            case LITERAL_null:
                return literalExpression(node, null);
            case STRING_LITERAL:
                return literalExpression(node, node.getText());

            case STRING_CONSTRUCTOR:
                return gstring(node);

            case NUM_DOUBLE:
            case NUM_FLOAT:
            case NUM_BIG_DECIMAL:
                return decimalExpression(node);

            case NUM_BIG_INT:
            case NUM_INT:
            case NUM_LONG:
                return integerExpression(node);

                // Unary expressions
            case LNOT:
                NotExpression notExpression = new NotExpression(expression(node.getFirstChild()));
                configureAST(notExpression, node);
                return notExpression;

            case UNARY_MINUS:
                return unaryMinusExpression(node);

            case BNOT:
                BitwiseNegationExpression bitwiseNegationExpression = new BitwiseNegationExpression(expression(node.getFirstChild()));
                configureAST(bitwiseNegationExpression, node);
                return bitwiseNegationExpression;

            case UNARY_PLUS:
                return unaryPlusExpression(node);

                // Prefix expressions
            case INC:
                return prefixExpression(node, Types.PLUS_PLUS);

            case DEC:
                return prefixExpression(node, Types.MINUS_MINUS);

                // Postfix expressions
            case POST_INC:
                return postfixExpression(node, Types.PLUS_PLUS);

            case POST_DEC:
                return postfixExpression(node, Types.MINUS_MINUS);


                // Binary expressions

            case ASSIGN:
                return binaryExpression(Types.ASSIGN, node);

            case EQUAL:
                return binaryExpression(Types.COMPARE_EQUAL, node);

            case NOT_EQUAL:
                return binaryExpression(Types.COMPARE_NOT_EQUAL, node);

            case COMPARE_TO:
                return binaryExpression(Types.COMPARE_TO, node);

            case LE:
                return binaryExpression(Types.COMPARE_LESS_THAN_EQUAL, node);

            case LT:
                return binaryExpression(Types.COMPARE_LESS_THAN, node);

            case GT:
                return binaryExpression(Types.COMPARE_GREATER_THAN, node);

            case GE:
                return binaryExpression(Types.COMPARE_GREATER_THAN_EQUAL, node);

                /**
                 * TODO treble equal?
                 return binaryExpression(Types.COMPARE_IDENTICAL, node);

                 case ???:
                 return binaryExpression(Types.LOGICAL_AND_EQUAL, node);

                 case ???:
                 return binaryExpression(Types.LOGICAL_OR_EQUAL, node);

                 */

            case LAND:
                return binaryExpression(Types.LOGICAL_AND, node);

            case LOR:
                return binaryExpression(Types.LOGICAL_OR, node);

            case BAND:
                return binaryExpression(Types.BITWISE_AND, node);

            case BAND_ASSIGN:
                return binaryExpression(Types.BITWISE_AND_EQUAL, node);

            case BOR:
                return binaryExpression(Types.BITWISE_OR, node);

            case BOR_ASSIGN:
                return binaryExpression(Types.BITWISE_OR_EQUAL, node);

            case BXOR:
                return binaryExpression(Types.BITWISE_XOR, node);

            case BXOR_ASSIGN:
                return binaryExpression(Types.BITWISE_XOR_EQUAL, node);


            case PLUS:
                return binaryExpression(Types.PLUS, node);

            case PLUS_ASSIGN:
                return binaryExpression(Types.PLUS_EQUAL, node);


            case MINUS:
                return binaryExpression(Types.MINUS, node);

            case MINUS_ASSIGN:
                return binaryExpression(Types.MINUS_EQUAL, node);


            case STAR:
                return binaryExpression(Types.MULTIPLY, node);

            case STAR_ASSIGN:
                return binaryExpression(Types.MULTIPLY_EQUAL, node);


            case STAR_STAR:
                return binaryExpression(Types.POWER, node);

            case STAR_STAR_ASSIGN:
                return binaryExpression(Types.POWER_EQUAL, node);


            case DIV:
                return binaryExpression(Types.DIVIDE, node);

            case DIV_ASSIGN:
                return binaryExpression(Types.DIVIDE_EQUAL, node);


            case MOD:
                return binaryExpression(Types.MOD, node);

            case MOD_ASSIGN:
                return binaryExpression(Types.MOD_EQUAL, node);

            case SL:
                return binaryExpression(Types.LEFT_SHIFT, node);

            case SL_ASSIGN:
                return binaryExpression(Types.LEFT_SHIFT_EQUAL, node);

            case SR:
                return binaryExpression(Types.RIGHT_SHIFT, node);

            case SR_ASSIGN:
                return binaryExpression(Types.RIGHT_SHIFT_EQUAL, node);

            case BSR:
                return binaryExpression(Types.RIGHT_SHIFT_UNSIGNED, node);

            case BSR_ASSIGN:
                return binaryExpression(Types.RIGHT_SHIFT_UNSIGNED_EQUAL, node);

            case VARIABLE_DEF:
                return declarationExpression(node);
                
                // Regex
            case REGEX_FIND:
                return binaryExpression(Types.FIND_REGEX, node);

            case REGEX_MATCH:
                return binaryExpression(Types.MATCH_REGEX, node);


                // Ranges
            case RANGE_INCLUSIVE:
                return rangeExpression(node, true);

            case RANGE_EXCLUSIVE:
                return rangeExpression(node, false);

            case DYNAMIC_MEMBER:
                return dynamicMemberExpression(node);
                
            case LITERAL_in:
                return binaryExpression(Types.KEYWORD_IN,node);
                
            case ANNOTATION:
                return new AnnotationConstantExpression(annotation(node));

            case CLOSURE_LIST:
                return closureListExpression(node);

            case LBRACK: case LPAREN:
                return tupleExpression(node);

            default:
                unknownAST(node);
        }
        return null;
    }

    private TupleExpression tupleExpression(AST node) {
        TupleExpression exp = new TupleExpression();
        configureAST(exp,node);
        node = node.getFirstChild();
        while (node!=null) {
            assertNodeType(VARIABLE_DEF,node);
            AST nameNode = node.getFirstChild().getNextSibling();
            VariableExpression varExp = new VariableExpression(nameNode.getText());
            configureAST(varExp,nameNode);
            exp.addExpression(varExp);
            node = node.getNextSibling();
        }
        return exp;
    }
    
    private ClosureListExpression closureListExpression(AST node) {
        AST exprNode = node.getFirstChild();
        LinkedList list = new LinkedList();
        while (exprNode!=null) {
            if (isType(EXPR,exprNode)) {
                Expression expr = expression(exprNode);
                configureAST(expr, exprNode);
                list.add(expr);                
            } else {
                assertNodeType(EMPTY_STAT, exprNode);
                list.add(EmptyExpression.INSTANCE);
            }
            
            exprNode = exprNode.getNextSibling();
        }
        ClosureListExpression cle = new ClosureListExpression(list);
        configureAST(cle,node);
        return cle;
    }

    protected Expression dynamicMemberExpression(AST dynamicMemberNode) {
        AST node = dynamicMemberNode.getFirstChild();
        return expression(node);
    }

    protected Expression ternaryExpression(AST ternaryNode) {
        AST node = ternaryNode.getFirstChild();
        Expression base = expression(node);
        node = node.getNextSibling();
        Expression left = expression(node);
        node = node.getNextSibling();
        Expression ret;
        if (node==null) {
            ret = new ElvisOperatorExpression(base, left);
        } else {
            Expression right = expression(node);
            BooleanExpression booleanExpression = new BooleanExpression(base);
            booleanExpression.setSourcePosition(base);
            ret = new TernaryExpression(booleanExpression, left, right);
        }
        configureAST(ret, ternaryNode);
        return ret;
    }

    protected Expression variableExpression(AST node) {
        String text = node.getText();

        // TODO we might wanna only try to resolve the name if we are
        // on the left hand side of an expression or before a dot?
        VariableExpression variableExpression = new VariableExpression(text);
        configureAST(variableExpression, node);
        return variableExpression;
    }

    protected Expression literalExpression(AST node, Object value) {
        ConstantExpression constantExpression = new ConstantExpression(value);
        configureAST(constantExpression, node);
        return constantExpression;
    }

    protected Expression rangeExpression(AST rangeNode, boolean inclusive) {
        AST node = rangeNode.getFirstChild();
        Expression left = expression(node);
        Expression right = expression(node.getNextSibling());
        RangeExpression rangeExpression = new RangeExpression(left, right, inclusive);
        configureAST(rangeExpression, rangeNode);
        return rangeExpression;
    }

    protected Expression spreadExpression(AST node) {
        AST exprNode = node.getFirstChild();
        AST listNode = exprNode.getFirstChild();
        Expression right = expression(listNode);
        SpreadExpression spreadExpression = new SpreadExpression(right);
        configureAST(spreadExpression, node);
        return spreadExpression;
    }

    protected Expression spreadMapExpression(AST node) {
        AST exprNode = node.getFirstChild();
        Expression expr = expression(exprNode);
        SpreadMapExpression spreadMapExpression = new SpreadMapExpression(expr);
        configureAST(spreadMapExpression, node);
        return spreadMapExpression;
    }

    protected Expression methodPointerExpression(AST node) {
        AST exprNode = node.getFirstChild();
        Expression objectExpression = expression(exprNode);
        AST mNode = exprNode.getNextSibling();
        Expression methodName;
        if (isType(DYNAMIC_MEMBER, mNode)) {
            methodName = expression(mNode);
        } else {
            methodName = new ConstantExpression(identifier(mNode));
        }
        configureAST(methodName,mNode);
        MethodPointerExpression methodPointerExpression = new MethodPointerExpression(objectExpression, methodName);
        configureAST(methodPointerExpression, node);
        return methodPointerExpression;
    }

/*  commented out due to groovy.g non-determinisms
  protected Expression defaultMethodPointerExpression(AST node) {
        AST exprNode = node.getFirstChild();
        String methodName = exprNode.toString();
        MethodPointerExpression methodPointerExpression = new MethodPointerExpression(null, methodName);
        configureAST(methodPointerExpression, node);
        return methodPointerExpression;
    }
*/

    protected Expression listExpression(AST listNode) {
        List expressions = new ArrayList();
        AST elist = listNode.getFirstChild();
        assertNodeType(ELIST, elist);

        for (AST node = elist.getFirstChild(); node != null; node = node.getNextSibling()) {
            // check for stray labeled arguments:
            switch (node.getType()) {
            case LABELED_ARG:       assertNodeType(COMMA, node);       break;  // helpful error?
            case SPREAD_MAP_ARG:    assertNodeType(SPREAD_ARG, node);  break;  // helpful error
            }
            expressions.add(expression(node));
        }
        ListExpression listExpression = new ListExpression(expressions);
        configureAST(listExpression, listNode);
        return listExpression;
    }

    /**
     * Typically only used for map constructors I think?
     */
    protected Expression mapExpression(AST mapNode) {
        List expressions = new ArrayList();
        AST elist = mapNode.getFirstChild();
        if (elist != null) {  // totally empty in the case of [:]
            assertNodeType(ELIST, elist);
            for (AST node = elist.getFirstChild(); node != null; node = node.getNextSibling()) {
                switch (node.getType()) {
                case LABELED_ARG:
                case SPREAD_MAP_ARG:
                    break;  // legal cases
                case SPREAD_ARG:
                    assertNodeType(SPREAD_MAP_ARG, node);  break;  // helpful error
                default:
                    assertNodeType(LABELED_ARG, node);  break;  // helpful error
                }
                expressions.add(mapEntryExpression(node));
            }
        }
        MapExpression mapExpression = new MapExpression(expressions);
        configureAST(mapExpression, mapNode);
        return mapExpression;
    }

    protected MapEntryExpression mapEntryExpression(AST node) {
        if (node.getType() == SPREAD_MAP_ARG) {
            AST rightNode = node.getFirstChild();
            Expression keyExpression = spreadMapExpression(node);
            Expression rightExpression = expression(rightNode);
            MapEntryExpression mapEntryExpression = new MapEntryExpression(keyExpression, rightExpression);
            configureAST(mapEntryExpression, node);
            return mapEntryExpression;
        }
        else {
            AST keyNode = node.getFirstChild();
            Expression keyExpression = expression(keyNode);
            AST valueNode = keyNode.getNextSibling();
            Expression valueExpression = expression(valueNode);
            MapEntryExpression mapEntryExpression = new MapEntryExpression(keyExpression, valueExpression);
            configureAST(mapEntryExpression, node);
            return mapEntryExpression;
        }
    }


    protected Expression instanceofExpression(AST node) {
        AST leftNode = node.getFirstChild();
        Expression leftExpression = expression(leftNode);

        AST rightNode = leftNode.getNextSibling();
        ClassNode type = buildName(rightNode);
        assertTypeNotNull(type, rightNode);

        Expression rightExpression = new ClassExpression(type);
        configureAST(rightExpression, rightNode);
        BinaryExpression binaryExpression = new BinaryExpression(leftExpression, makeToken(Types.KEYWORD_INSTANCEOF, node), rightExpression);
        configureAST(binaryExpression, node);
        return binaryExpression;
    }

    protected void assertTypeNotNull(ClassNode type, AST rightNode) {
        if (type == null) {
            throw new ASTRuntimeException(rightNode, "No type available for: " + qualifiedName(rightNode));
        }
    }

    protected Expression asExpression(AST node) {
        AST leftNode = node.getFirstChild();
        Expression leftExpression = expression(leftNode);

        AST rightNode = leftNode.getNextSibling();
        ClassNode type = buildName(rightNode);

        return CastExpression.asExpression(type, leftExpression);
    }

    protected Expression castExpression(AST castNode) {
        AST node = castNode.getFirstChild();
        ClassNode type = buildName(node);
        assertTypeNotNull(type, node);

        AST expressionNode = node.getNextSibling();
        Expression expression = expression(expressionNode);

        CastExpression castExpression = new CastExpression(type, expression);
        configureAST(castExpression, castNode);
        return castExpression;
    }


    protected Expression indexExpression(AST indexNode) {
        AST leftNode = indexNode.getFirstChild();
        Expression leftExpression = expression(leftNode);

        AST rightNode = leftNode.getNextSibling();
        Expression rightExpression = expression(rightNode);

        BinaryExpression binaryExpression = new BinaryExpression(leftExpression, makeToken(Types.LEFT_SQUARE_BRACKET, indexNode), rightExpression);
        configureAST(binaryExpression, indexNode);
        return binaryExpression;
    }

    protected Expression binaryExpression(int type, AST node) {
        Token token = makeToken(type, node);

        AST leftNode = node.getFirstChild();
        Expression leftExpression = expression(leftNode);

        AST rightNode = leftNode.getNextSibling();
        if (rightNode == null) {
            return leftExpression;
        }

        if (Types.ofType(type, Types.ASSIGNMENT_OPERATOR)) {
            if (leftExpression instanceof VariableExpression || 
                leftExpression.getClass() == PropertyExpression.class ||
                leftExpression instanceof FieldExpression ||
                leftExpression instanceof AttributeExpression ||
                leftExpression instanceof DeclarationExpression ||
                leftExpression instanceof TupleExpression)
            {
                // Do nothing.
            }
            else if (leftExpression instanceof ConstantExpression) {
                throw new ASTRuntimeException(node, "\n[" + ((ConstantExpression) leftExpression).getValue() + "] is a constant expression, but it should be a variable expression");
            }
            else if (leftExpression instanceof BinaryExpression) {
                Expression leftexp = ((BinaryExpression) leftExpression).getLeftExpression();
                int lefttype = ((BinaryExpression) leftExpression).getOperation().getType();
                if (!Types.ofType(lefttype, Types.ASSIGNMENT_OPERATOR) && lefttype != Types.LEFT_SQUARE_BRACKET) {
                    throw new ASTRuntimeException(node, "\n" + ((BinaryExpression) leftExpression).getText() + " is a binary expression, but it should be a variable expression");
                }
            }
            else if (leftExpression instanceof GStringExpression) {
                throw new ASTRuntimeException(node, "\n\"" + ((GStringExpression) leftExpression).getText() + "\" is a GString expression, but it should be a variable expression");
            }
            else if (leftExpression instanceof MethodCallExpression) {
                throw new ASTRuntimeException(node, "\n\"" + ((MethodCallExpression) leftExpression).getText() + "\" is a method call expression, but it should be a variable expression");
            }
            else if (leftExpression instanceof MapExpression) {
                throw new ASTRuntimeException(node, "\n'" + ((MapExpression) leftExpression).getText() + "' is a map expression, but it should be a variable expression");
            }
            else {
                throw new ASTRuntimeException(node, "\n" + leftExpression.getClass() + ", with its value '" + leftExpression.getText() + "', is a bad expression as the left hand side of an assignment operator");
            }
        }
        /*if (rightNode == null) {
            throw new NullPointerException("No rightNode associated with binary expression");
        }*/
        Expression rightExpression = expression(rightNode);
        BinaryExpression binaryExpression = new BinaryExpression(leftExpression, token, rightExpression);
        configureAST(binaryExpression, node);
        return binaryExpression;
    }

    protected Expression prefixExpression(AST node, int token) {
        Expression expression = expression(node.getFirstChild());
        PrefixExpression prefixExpression = new PrefixExpression(makeToken(token, node), expression);
        configureAST(prefixExpression, node);
        return prefixExpression;
    }

    protected Expression postfixExpression(AST node, int token) {
        Expression expression = expression(node.getFirstChild());
        PostfixExpression postfixExpression = new PostfixExpression(expression, makeToken(token, node));
        configureAST(postfixExpression, node);
        return postfixExpression;
    }

    protected BooleanExpression booleanExpression(AST node) {
        BooleanExpression booleanExpression = new BooleanExpression(expression(node));
        configureAST(booleanExpression, node);
        return booleanExpression;
    }

    protected Expression dotExpression(AST node) {
        // let's decide if this is a property invocation or a method call
        AST leftNode = node.getFirstChild();
        if (leftNode != null) {
            AST identifierNode = leftNode.getNextSibling();
            if (identifierNode != null) {
                Expression leftExpression = expression(leftNode);
                if (isType(SELECT_SLOT, identifierNode)) {
                    Expression field = expression(identifierNode.getFirstChild(),true);
                    AttributeExpression attributeExpression = new AttributeExpression(leftExpression, field, node.getType() != DOT);
                    if (node.getType() == SPREAD_DOT) {
                        attributeExpression.setSpreadSafe(true);
                    }
                    configureAST(attributeExpression, node);
                    return attributeExpression;
                }
                Expression property = expression(identifierNode,true);
                
                PropertyExpression propertyExpression = new PropertyExpression(leftExpression, property, node.getType() != DOT);
                if (node.getType() == SPREAD_DOT) {
                    propertyExpression.setSpreadSafe(true);
                } 
                configureAST(propertyExpression, node);
                return propertyExpression;
            }
        }
        return methodCallExpression(node);
    }
    
    protected Expression specialConstructorCallExpression(AST methodCallNode, ClassNode special) {
        AST node = methodCallNode.getFirstChild();
        Expression arguments = arguments(node);
        
        ConstructorCallExpression expression = new ConstructorCallExpression(special, arguments);
        configureAST(expression, methodCallNode);
        return expression;
    }

    private int getTypeInParenthesis(AST node) {
        if (! isType(EXPR,node) ) node = node.getFirstChild();
        while (node!=null &&isType(EXPR,node) && node.getNextSibling()==null) {
            node = node.getFirstChild();
        }
        if (node==null) return -1;
        return node.getType();
    }

    protected Expression methodCallExpression(AST methodCallNode) {
        AST node = methodCallNode.getFirstChild();
        /* // Bad idea, since foo(1)(2) is valid Groovy for foo(1).call(2).
        if (isType(METHOD_CALL, node)) {
            // sometimes method calls get wrapped in method calls for some wierd reason
            return methodCallExpression(node);
        }
        */

        Expression objectExpression;
        AST selector;
        AST elist = node.getNextSibling();
        
        boolean implicitThis = false;
        boolean safe = isType(OPTIONAL_DOT, node);
        boolean spreadSafe = isType(SPREAD_DOT, node);
        if (isType(DOT, node) || safe || spreadSafe) {
            AST objectNode = node.getFirstChild();
            objectExpression = expression(objectNode);
            selector = objectNode.getNextSibling();
        } else {
            implicitThis = true;
            objectExpression = VariableExpression.THIS_EXPRESSION;
            selector = node;
        } 

        Expression name = null;
        if (isType(LITERAL_super, selector)) {
            implicitThis = true;
            name = new ConstantExpression("super");
            if (objectExpression instanceof VariableExpression && ((VariableExpression)objectExpression).isThisExpression()) {
                objectExpression = VariableExpression.SUPER_EXPRESSION;
            }
        } else if (isPrimitiveTypeLiteral(selector)) {
            throw new ASTRuntimeException(selector, "Primitive type literal: " + selector.getText()
                    + " cannot be used as a method name");
        } else if (isType(SELECT_SLOT, selector)) {
            Expression field = expression(selector.getFirstChild(),true);
            AttributeExpression attributeExpression = new AttributeExpression(objectExpression, field, node.getType() != DOT);
            configureAST(attributeExpression, node);
            Expression arguments = arguments(elist);
            MethodCallExpression expression = new MethodCallExpression(attributeExpression, "call", arguments);
            configureAST(expression, methodCallNode);
            return expression;
        } else if  
               (isType(DYNAMIC_MEMBER, selector) || isType(IDENT,selector) || 
                isType(STRING_CONSTRUCTOR,selector) || isType (STRING_LITERAL,selector)) 
        { 
            name = expression(selector,true);
        } else {
            implicitThis = false;
            name = new ConstantExpression("call");
            objectExpression = expression(selector,true);
        } 

        // if node text is found to be "super"/"this" when a method call is being processed, it is a 
        // call like this(..)/super(..) after the first statement, which shouldn't be allowed. GROOVY-2836
        if(selector.getText().equals("this") || selector.getText().equals("super")) {
        	throw new ASTRuntimeException(elist, "Constructor call must be the first statement in a constructor.");
        }
        
        Expression arguments = arguments(elist);
        MethodCallExpression expression = new MethodCallExpression(objectExpression, name, arguments);
        expression.setSafe(safe);
        expression.setSpreadSafe(spreadSafe);
        expression.setImplicitThis(implicitThis);
        Expression ret = expression;
        //FIXME: do we really want this() to create a new object regardless
        // the position.. for example not as first statement in a constructor
        // this=first statement in constructor is handled by specialConstructorCallExpression
        // we may have to add a check and remove this part of the code
        if (implicitThis && "this".equals(expression.getMethodAsString())) {
            ret = new ConstructorCallExpression(this.classNode, arguments);
        }
        configureAST(ret, methodCallNode);
        return ret;
    }
    
    protected Expression constructorCallExpression(AST node) {
        AST constructorCallNode = node;
        ClassNode type = makeTypeWithArguments(constructorCallNode);

        if (isType(CTOR_CALL, node) || isType(LITERAL_new, node)) {
            node = node.getFirstChild();
        }

        AST elist = node.getNextSibling();

        if (elist == null && isType(ELIST, node)) {
            elist = node;
            if ("(".equals(type.getName())) {
                type = classNode;
            }
        }

        if (isType(ARRAY_DECLARATOR, elist)) {
            AST expressionNode = elist.getFirstChild();
            if (expressionNode == null) {
                throw new ASTRuntimeException(elist, "No expression for the array constructor call");
            }
            List size = arraySizeExpression(expressionNode);
            ArrayExpression arrayExpression = new ArrayExpression(type, null, size);
            configureAST(arrayExpression, constructorCallNode);
            return arrayExpression;
        }
        Expression arguments = arguments(elist);
        ConstructorCallExpression expression = new ConstructorCallExpression(type, arguments);
        configureAST(expression, constructorCallNode);
        return expression;
    }
    
    protected List arraySizeExpression(AST node) {
        List list;
        Expression size = null;
    	if (isType(ARRAY_DECLARATOR,node)) {
    		AST right = node.getNextSibling();
        	if (right!=null) {
        		size = expression(right);
        	} else {
        		size = ConstantExpression.EMTPY_EXPRESSION;
        	}
        	list = arraySizeExpression(node.getFirstChild());
        } else {
        	size = expression(node);
        	list = new ArrayList();
        }
    	list.add(size);
    	return list;
    }

    protected Expression arguments(AST elist) {
        List expressionList = new ArrayList();
        // FIXME: all labeled arguments should follow any unlabeled arguments
        boolean namedArguments = false;
        for (AST node = elist; node != null; node = node.getNextSibling()) {
            if (isType(ELIST, node)) {
                for (AST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    namedArguments |= addArgumentExpression(child, expressionList);
                }
            }
            else {
                namedArguments |= addArgumentExpression(node, expressionList);
            }
        }
        if (namedArguments) {
            if (!expressionList.isEmpty()) {
                // let's remove any non-MapEntryExpression instances
                // such as if the last expression is a ClosureExpression
                // so let's wrap the named method calls in a Map expression
                List argumentList = new ArrayList();
                for (Iterator iter = expressionList.iterator(); iter.hasNext();) {
                    Expression expression = (Expression) iter.next();
                    if (!(expression instanceof MapEntryExpression)) {
                        argumentList.add(expression);
                    }
                }
                if (!argumentList.isEmpty()) {
                    expressionList.removeAll(argumentList);
                    MapExpression mapExpression = new MapExpression(expressionList);
                    configureAST(mapExpression, elist);
                    argumentList.add(0, mapExpression);
                    ArgumentListExpression argumentListExpression = new ArgumentListExpression(argumentList);
                    configureAST(argumentListExpression, elist);
                    return argumentListExpression;
                }
            }
            NamedArgumentListExpression namedArgumentListExpression = new NamedArgumentListExpression(expressionList);
            configureAST(namedArgumentListExpression, elist);
            return namedArgumentListExpression;
        }
        else {
            ArgumentListExpression argumentListExpression = new ArgumentListExpression(expressionList);
            configureAST(argumentListExpression, elist);
            return argumentListExpression;
        }
    }

    protected boolean addArgumentExpression(AST node, List expressionList) {
        if (node.getType() == SPREAD_MAP_ARG) {
            AST rightNode = node.getFirstChild();
            Expression keyExpression = spreadMapExpression(node);
            Expression rightExpression = expression(rightNode);
            MapEntryExpression mapEntryExpression = new MapEntryExpression(keyExpression, rightExpression);
            expressionList.add(mapEntryExpression);
            return true;
        }
        else {
            Expression expression = expression(node);
            expressionList.add(expression);
            return expression instanceof MapEntryExpression;
        }
    }

    protected Expression expressionList(AST node) {
        List expressionList = new ArrayList();
        for (AST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            expressionList.add(expression(child));
        }
        if (expressionList.size() == 1) {
            return (Expression) expressionList.get(0);
        } else {
            ListExpression listExpression = new ListExpression(expressionList);
            listExpression.setWrapped(true);
            configureAST(listExpression, node);
            return listExpression;
        }
    }

    protected ClosureExpression closureExpression(AST node) {
        AST paramNode = node.getFirstChild();
        Parameter[] parameters = null;
        AST codeNode = paramNode;
        if (isType(PARAMETERS, paramNode) || isType(IMPLICIT_PARAMETERS, paramNode)) {
            parameters = parameters(paramNode);
            codeNode = paramNode.getNextSibling();
        }
        Statement code = statementListNoChild(codeNode,node);
        ClosureExpression closureExpression = new ClosureExpression(parameters, code);
        configureAST(closureExpression, node);
        return closureExpression;
    }

    protected Expression blockExpression(AST node) {
        AST codeNode = node.getFirstChild();
        if (codeNode == null)  return ConstantExpression.NULL;
        if (codeNode.getType() == EXPR && codeNode.getNextSibling() == null) {
            // Simplify common case of {expr} to expr.
            return expression(codeNode);
        }
        Parameter[] parameters = Parameter.EMPTY_ARRAY;
        Statement code = statementListNoChild(codeNode,node);
        ClosureExpression closureExpression = new ClosureExpression(parameters, code);
        configureAST(closureExpression, node);
        // Call it immediately.
        String callName = "call";
        Expression noArguments = new ArgumentListExpression();
        MethodCallExpression call = new MethodCallExpression(closureExpression, callName, noArguments);
        configureAST(call, node);
        return call;
    }

    protected Expression unaryMinusExpression(AST unaryMinusExpr) {
        AST node = unaryMinusExpr.getFirstChild();

        // if we are a number literal then let's just parse it
        // as the negation operator on MIN_INT causes rounding to a long
        String text = node.getText();
        switch (node.getType()) {
            case NUM_DOUBLE:
            case NUM_FLOAT:
            case NUM_BIG_DECIMAL:
                ConstantExpression constantExpression = new ConstantExpression(Numbers.parseDecimal("-" + text));
                configureAST(constantExpression, unaryMinusExpr);
                return constantExpression;

            case NUM_BIG_INT:
            case NUM_INT:
            case NUM_LONG:
                ConstantExpression constantLongExpression = new ConstantExpression(Numbers.parseInteger("-" + text));
                configureAST(constantLongExpression, unaryMinusExpr);
                return constantLongExpression;

            default:
                UnaryMinusExpression unaryMinusExpression = new UnaryMinusExpression(expression(node));
                configureAST(unaryMinusExpression, unaryMinusExpr);
                return unaryMinusExpression;
        }
    }

    protected Expression unaryPlusExpression(AST unaryPlusExpr) {
        AST node = unaryPlusExpr.getFirstChild();
        switch (node.getType()) {
            case NUM_DOUBLE:
            case NUM_FLOAT:
            case NUM_BIG_DECIMAL:
            case NUM_BIG_INT:
            case NUM_INT:
            case NUM_LONG:
                return expression(node);

            default:
                UnaryPlusExpression unaryPlusExpression = new UnaryPlusExpression(expression(node));
                configureAST(unaryPlusExpression, unaryPlusExpr);
                return unaryPlusExpression;
        }
    }

    protected ConstantExpression decimalExpression(AST node) {
        String text = node.getText();
        ConstantExpression constantExpression = new ConstantExpression(Numbers.parseDecimal(text));
        configureAST(constantExpression, node);
        return constantExpression;
    }

    protected ConstantExpression integerExpression(AST node) {
        String text = node.getText();
        ConstantExpression constantExpression = new ConstantExpression(Numbers.parseInteger(text));
        configureAST(constantExpression, node);
        return constantExpression;
    }

    protected Expression gstring(AST gstringNode) {
        List strings = new ArrayList();
        List values = new ArrayList();

        StringBuffer buffer = new StringBuffer();

        boolean isPrevString = false;

        for (AST node = gstringNode.getFirstChild(); node != null; node = node.getNextSibling()) {
            int type = node.getType();
            String text = null;
            switch (type) {

                case STRING_LITERAL:
                    if (isPrevString)  assertNodeType(IDENT, node);  // parser bug
                    isPrevString = true;
                    text = node.getText();
                    ConstantExpression constantExpression = new ConstantExpression(text);
                    configureAST(constantExpression, node);
                    strings.add(constantExpression);
                    buffer.append(text);
                    break;

                default:
                    {
                        if (!isPrevString)  assertNodeType(IDENT, node);  // parser bug
                        isPrevString = false;
                        Expression expression = expression(node);
                        values.add(expression);
                        buffer.append("$");
                        buffer.append(expression.getText());
                    }
                    break;
            }
        }
        GStringExpression gStringExpression = new GStringExpression(buffer.toString(), strings, values);
        configureAST(gStringExpression, gstringNode);
        return gStringExpression;
    }

    protected ClassNode type(AST typeNode) {
        // TODO intern types?
        // TODO configureAST(...)
        return buildName(typeNode.getFirstChild());
    }

    public static String qualifiedName(AST qualifiedNameNode) {
    	if (isType(IDENT, qualifiedNameNode)) {
        	return qualifiedNameNode.getText();
        }
        if (isType(DOT, qualifiedNameNode)) {
            AST node = qualifiedNameNode.getFirstChild();
            StringBuffer buffer = new StringBuffer();
            boolean first = true;

            for (; node != null && !isType(TYPE_ARGUMENTS,node); node = node.getNextSibling()) {
                if (first) {
                    first = false;
                }
                else {
                    buffer.append(".");
                }
                buffer.append(qualifiedName(node));
            }
            return buffer.toString();
        }
        else {
            return qualifiedNameNode.getText();
        }
    }
    
    private static AST getTypeArgumentsNode(AST root) {
        while (root!=null && !isType(TYPE_ARGUMENTS,root)) {
            root = root.getNextSibling();
        }
        return root;
    }
    
    private int getBoundType(AST node) {
        if (node==null) return -1;
        if (isType(TYPE_UPPER_BOUNDS,node)) return TYPE_UPPER_BOUNDS;
        if (isType(TYPE_LOWER_BOUNDS,node)) return TYPE_LOWER_BOUNDS;
        throw new ASTRuntimeException(node, 
                "Unexpected node type: " + getTokenName(node) + 
                " found when expecting type: " + getTokenName(TYPE_UPPER_BOUNDS) +
                " or type: " + getTokenName(TYPE_LOWER_BOUNDS));       
    }
    
    private GenericsType makeGenericsArgumentType(AST typeArgument) {
        GenericsType gt;
        AST rootNode = typeArgument.getFirstChild();
        if (isType(WILDCARD_TYPE,rootNode)) {
            ClassNode base = ClassHelper.makeWithoutCaching("?");
            if (rootNode.getNextSibling()!=null) {
                int boundType = getBoundType(rootNode.getNextSibling());
                ClassNode[] gts = makeGenericsBounds(rootNode,boundType);
                if (boundType==TYPE_UPPER_BOUNDS) {
                    gt = new GenericsType(base,gts,null);
                } else {
                    gt = new GenericsType(base,null,gts[0]);
                }
            } else {
                gt = new GenericsType(base,null,null);
            }
            gt.setName("?");
            gt.setWildcard(true);
        } else {
            ClassNode argument = makeTypeWithArguments(rootNode);
            gt = new GenericsType(argument);
        }
        configureAST(gt, typeArgument);
        return gt;
    }
    
    protected ClassNode makeTypeWithArguments(AST rootNode) {
        ClassNode basicType = makeType(rootNode);
        LinkedList typeArgumentList = new LinkedList();
        AST node = rootNode.getFirstChild();
        if (node==null || isType(INDEX_OP, node) || isType(ARRAY_DECLARATOR, node)) return basicType;
        //TODO: recognize combinatons of inner classes and generic types
        if (isType(DOT, node)) return basicType;
        node = node.getFirstChild();
        if (node==null) return basicType;
        assertNodeType(TYPE_ARGUMENTS, node);
        AST typeArgument = node.getFirstChild();
        
        while (typeArgument != null) {
            assertNodeType(TYPE_ARGUMENT, typeArgument);            
            GenericsType gt = makeGenericsArgumentType(typeArgument);
            typeArgumentList.add(gt);
            typeArgument = typeArgument.getNextSibling();
        }
        
        if (typeArgumentList.size()>0) {
            basicType.setGenericsTypes((GenericsType[]) typeArgumentList.toArray(new GenericsType[0]));
        }
        return basicType;
    }
    
    private ClassNode[] makeGenericsBounds(AST rn, int boundType) {
        AST boundsRoot = rn.getNextSibling();
        if (boundsRoot==null) return null;
        assertNodeType(boundType, boundsRoot);
        LinkedList bounds = new LinkedList();
        for ( AST boundsNode = boundsRoot.getFirstChild(); 
              boundsNode!=null; 
              boundsNode=boundsNode.getNextSibling()
        ) {
            ClassNode bound = null;
            bound = makeTypeWithArguments(boundsNode);
            configureAST(bound, boundsNode);
            bounds.add(bound);
        }
        if (bounds.size()==0) return null;
        return (ClassNode[]) bounds.toArray(new ClassNode[bounds.size()]);
    }
    
    protected GenericsType[] makeGenericsType(AST rootNode) {
        AST typeParameter = rootNode.getFirstChild();
        LinkedList ret = new LinkedList();
        assertNodeType(TYPE_PARAMETER, typeParameter);

        while (isType(TYPE_PARAMETER, typeParameter)) {
            AST typeNode = typeParameter.getFirstChild();
            ClassNode type = makeType(typeParameter);
            
            GenericsType gt = new GenericsType(type, makeGenericsBounds(typeNode,TYPE_UPPER_BOUNDS),null);
            configureAST(gt, typeParameter);
            
            ret.add(gt);
            typeParameter = typeParameter.getNextSibling();
        }
        return (GenericsType[]) ret.toArray(new GenericsType[0]);
    }
    
    protected ClassNode makeType(AST typeNode) {
        ClassNode answer = ClassHelper.DYNAMIC_TYPE;
        AST node = typeNode.getFirstChild();
        if (node != null) {
            if (isType(INDEX_OP, node) || isType(ARRAY_DECLARATOR, node)) {
                answer = makeType(node).makeArray();
            } else {
                answer = ClassHelper.make(qualifiedName(node));
            }
            configureAST(answer,node);
        }
        return answer;
    }

    /**
     * Performs a name resolution to see if the given name is a type from imports,
     * aliases or newly created classes
     */
    /*protected String resolveTypeName(String name, boolean safe) {
        if (name == null) {
            return null;
        }
        return resolveNewClassOrName(name, safe);
    }*/

    /**
     * Extracts an identifier from the Antlr AST and then performs a name resolution
     * to see if the given name is a type from imports, aliases or newly created classes
     */
    protected ClassNode buildName(AST node) {
        if (isType(TYPE, node)) {
            node = node.getFirstChild();
        }
        ClassNode answer = null;
        if (isType(DOT, node) || isType(OPTIONAL_DOT, node)) {
            answer = ClassHelper.make(qualifiedName(node));
        }
        else if (isPrimitiveTypeLiteral(node)) {
            answer = ClassHelper.make(node.getText());
        }
        else if (isType(INDEX_OP, node) || isType(ARRAY_DECLARATOR, node)) {
            AST child = node.getFirstChild();
            answer = buildName(child).makeArray();
            configureAST(answer, node);
            return answer;
        }
        else {
            String identifier = node.getText();
            answer = ClassHelper.make(identifier);
        }
        AST nextSibling = node.getNextSibling();
        if (isType(INDEX_OP, nextSibling) || isType(ARRAY_DECLARATOR, node)) {
        	answer = answer.makeArray();
        	configureAST(answer, node);
            return answer;
        }
        else {
        	configureAST(answer, node);
            return answer;
        }
    }

    protected boolean isPrimitiveTypeLiteral(AST node) {
        int type = node.getType();
        switch (type) {
            case LITERAL_boolean:
            case LITERAL_byte:
            case LITERAL_char:
            case LITERAL_double:
            case LITERAL_float:
            case LITERAL_int:
            case LITERAL_long:
            case LITERAL_short:
                return true;

            default:
                return false;
        }
    }

    /**
     * Extracts an identifier from the Antlr AST
     */
    protected String identifier(AST node) {
        assertNodeType(IDENT, node);
        return node.getText();
    }

    protected String label(AST labelNode) {
        AST node = labelNode.getFirstChild();
        if (node == null) {
            return null;
        }
        return identifier(node);
    }



    // Helper methods
    //-------------------------------------------------------------------------


    /**
     * Returns true if the modifiers flags contain a visibility modifier
     */
    protected boolean hasVisibility(int modifiers) {
        return (modifiers & (Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED | Opcodes.ACC_PUBLIC)) != 0;
    }

    protected void configureAST(ASTNode node, AST ast) {
        if (ast==null) throw new ASTRuntimeException(ast, "PARSER BUG: Tried to configure "+node.getClass().getName()+" with null Node");
        node.setColumnNumber(ast.getColumn());
        node.setLineNumber(ast.getLine());
        if (ast instanceof GroovySourceAST) {
            node.setLastColumnNumber(((GroovySourceAST)ast).getColumnLast());
            node.setLastLineNumber(((GroovySourceAST)ast).getLineLast());
        }

        // TODO we could one day store the Antlr AST on the Groovy AST
        // node.setCSTNode(ast);
    }

    protected static Token makeToken(int typeCode, AST node) {
        return Token.newSymbol(typeCode, node.getLine(), node.getColumn());
    }

    protected String getFirstChildText(AST node) {
        AST child = node.getFirstChild();
        return child != null ? child.getText() : null;
    }


    public static boolean isType(int typeCode, AST node) {
        return node != null && node.getType() == typeCode;
    }

    private String getTokenName(int token) {
        if (tokenNames==null) return ""+token;
        return tokenNames[token];
    }
    
    private String getTokenName(AST node) {
        if (node==null) return "null";
        return getTokenName(node.getType());
    }

    protected void assertNodeType(int type, AST node) {
        if (node == null) {
            throw new ASTRuntimeException(node, "No child node available in AST when expecting type: " + getTokenName(type));
        }
        if (node.getType() != type) {            
            throw new ASTRuntimeException(node, "Unexpected node type: " + getTokenName(node) + " found when expecting type: " + getTokenName(type));
        }
    }

    protected void notImplementedYet(AST node) {
        throw new ASTRuntimeException(node, "AST node not implemented yet for type: " + getTokenName(node));
    }

    protected void unknownAST(AST node) {
        if (node.getType() == CLASS_DEF) {
            throw new ASTRuntimeException(node,
                    "Class definition not expected here. Possible attempt to use inner class. " +
                            "Inner classes not supported, perhaps try using a closure instead.");
        }
        throw new ASTRuntimeException(node, "Unknown type: " + getTokenName(node));
    }

    protected void dumpTree(AST ast) {
        for (AST node = ast.getFirstChild(); node != null; node = node.getNextSibling()) {
            dump(node);
        }
    }

    protected void dump(AST node) {
        System.out.println("Type: " + getTokenName(node) + " text: " + node.getText());
    }
}
