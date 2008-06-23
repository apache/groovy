package org.codehaus.groovy.classgen;

import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.objectweb.asm.util.ASMifierClassVisitor;

import java.lang.ref.SoftReference;

public class JO {
    public static SoftReference staticMetaClass;

    MetaClass getStaticMetaClass (Object obj) {
        MetaClass mc;
        if (staticMetaClass == null || (mc = (MetaClass) staticMetaClass.get()) == null ) {
            mc = InvokerHelper.getMetaClass(obj);
            staticMetaClass = new SoftReference(mc);
        }
        return mc;
    }

    public static void main(String[] args) throws Exception {
        ASMifierClassVisitor.main(new String[]{"target/classes/groovy/swing/SwingBuilder.class"});
//        ASMifierClassVisitor.main(new String[]{"target/classes/org/codehaus/groovy/tools/shell/util/HelpFormatter.class"});
//        ASMifierClassVisitor.main(new String[]{"target/classes/org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite.class"});
//        ASMifierClassVisitor.main(new String[]{"target/test-classes/spectralnorm.class"});
//        ASMifierClassVisitor.main(new String[]{"target/test-classes/groovy/bugs/CustomMetaClassTest.class"});
    }
}
