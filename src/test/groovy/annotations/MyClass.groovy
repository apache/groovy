@MyIntegerAnno(40) // ignored at runtime
package groovy.annotations

@MyIntegerAnno(50) // ignored at runtime
import java.util.concurrent.atomic.AtomicInteger

@MyIntegerAnno(60)
class MyClass {
    AtomicInteger dontUse = null
}