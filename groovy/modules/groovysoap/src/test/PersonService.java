/*
 * NewInterface.java
 *
 * Created on 13 février 2006, 16:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test;

/**
 *
 * @author Guillaume Alleon
 */
public interface PersonService {
    Person findPerson(Integer id);
    Person[] getPersons();
}
