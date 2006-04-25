package groovy.net.soap;
/*
 * PublicMethodNotFoundException.java
 *
 * Created on 23 avril 2006, 08:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author SU16766
 */
public class PublicMethodNotFoundException extends RuntimeException {
    
    /** Creates a new instance of PublicMethodNotFoundException */
    public PublicMethodNotFoundException(String message) {
        super(message);
    }
    
    public PublicMethodNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public String getMessage() {
        return super.getMessage() + getLocationText();
    }
    
    protected String getLocationText() {
        String answer = ". ";
        
        if (answer.equals(". ")) {
            return "";
        }
        return answer;
    }
}
