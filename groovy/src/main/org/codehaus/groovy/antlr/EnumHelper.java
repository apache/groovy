package org.codehaus.groovy.antlr;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MixinNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.objectweb.asm.Opcodes;


public class EnumHelper {
    private static final int FS = Opcodes.ACC_FINAL | Opcodes.ACC_STATIC;
    private static final int PUBLIC_FS = Opcodes.ACC_PUBLIC | FS; 
    
    public static ClassNode makeEnumNode(String name, int modifiers, ClassNode[] interfaces, ClassNode outerClass) {
        modifiers = modifiers | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM;
        ClassNode enumClass;
        if (outerClass==null) {
            enumClass = new ClassNode(name,modifiers,null,interfaces,MixinNode.EMPTY_ARRAY);
        } else {
            name = outerClass.getName() + "$" + name;
            enumClass = new InnerClassNode(outerClass,name,modifiers,null,interfaces,MixinNode.EMPTY_ARRAY);
        }
        
        // set super class and generics info
        // "enum X" -> class X extends Enum<X>
        GenericsType gt = new GenericsType(enumClass);
        ClassNode superClass = ClassHelper.makeWithoutCaching("java.lang.Enum");
        superClass.setGenericsTypes(new GenericsType[]{gt});
        enumClass.setSuperClass(superClass);
        superClass.setRedirect(ClassHelper.Enum_Type);
        
        return enumClass;
    }

    public static void addEnumConstant(ClassNode enumClass, String name, Expression init) {
        int modifiers = PUBLIC_FS | Opcodes.ACC_ENUM;
        if  (init!=null && !(init instanceof ListExpression)) {
            ListExpression list = new ListExpression();
            list.addExpression(init);
            init = list;
        }
        FieldNode fn = new FieldNode(name,modifiers,enumClass,enumClass,init);
        enumClass.addField(fn);
    }
}
