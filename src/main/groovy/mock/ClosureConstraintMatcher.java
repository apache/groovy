package groovy.mock;

import groovy.lang.Closure;

import com.mockobjects.constraint.Constraint;

/**
 * 
 * @author Joe Walnes
 * @author Chris Stevenson
 * @version $Revision$
 */
public class ClosureConstraintMatcher implements Constraint {
    private Closure closure;
    private String message = "closure";

    public ClosureConstraintMatcher(Closure closure) {
        this.closure = closure;
    }

    public boolean eval(Object object) {
        try {
            closure.call(object);
            return true;
        }
        catch (AssertionError e) {
            message = e.getMessage();
            return false;
        }
    }

    public String toString() {
        return message;
    }

}
