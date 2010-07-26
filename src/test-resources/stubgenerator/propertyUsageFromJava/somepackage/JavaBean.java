package somepackage;

public class JavaBean {
    public void usePogo() {
        GroovyPogo pogo = new GroovyPogo();
        pogo.setName("Guillaume");
        pogo.setAge(33);
        System.out.println(pogo.getName());
        System.out.println(pogo.getAge());
    }
}
