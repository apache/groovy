package groovy.bugs;

import org.objectweb.asm.Opcodes;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.transform.*;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class G3839Transform2 implements ASTTransformation, Opcodes{

    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        ClassNode classNode = (ClassNode) nodes[1];
        classNode.addField(new FieldNode("f2", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, classNode, null));
    }

}
