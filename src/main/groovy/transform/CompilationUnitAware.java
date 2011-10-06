package groovy.transform;
/**
 * Created by IntelliJ IDEA.
 * User: cedric
 * Date: 06/10/11
 * Time: 15:02
 */

import org.codehaus.groovy.control.CompilationUnit;

/**
 * This interface is for AST transformations which must be aware of the compilation unit where they are applied.
 *
 * @author Cedric Champeau
 */
public interface CompilationUnitAware {
    void setCompilationUnit(CompilationUnit unit);
}
