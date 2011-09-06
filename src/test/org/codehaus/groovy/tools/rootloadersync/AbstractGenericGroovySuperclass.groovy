package org.codehaus.groovy.tools.rootloadersync

public abstract class AbstractGenericGroovySuperclass<T> {
   private Set<T> notes;

   public AbstractGenericGroovySuperclass(Set<T> notes) {
      this.notes = notes;
   }

   public void addNote(T note) {
      doSomething(note);
      notes.add(note);
   }

   protected abstract void doSomething(T note);
}
