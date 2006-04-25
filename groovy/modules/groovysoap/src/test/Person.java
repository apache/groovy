/*
 * Person.java
 *
 * Created on 13 février 2006, 13:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test;

/**
 *
 * @author SU16766
 */
public class Person {
    private String firstName;
    private String lastName;
    private Integer id;
    
    public String getFirstName(){
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName(){
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
        
    public Integer getId(){
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String toString() {
        return "["+id+"] "+firstName+" "+lastName;
    }
    
}
