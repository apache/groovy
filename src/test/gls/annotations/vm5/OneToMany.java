package gls.annotations.vm5;

import java.lang.annotation.*;

import static java.lang.annotation.RetentionPolicy.*;
import static java.lang.annotation.ElementType.*;

/**
 * This class mimicks JPA's @OneToMany annotation
 *
 * @author Guillaume Laforge
 */

@Retention(RUNTIME)
@Target({ METHOD, FIELD })
public @interface OneToMany {
    public CascadeType[] cascade() default {};
}
