package io.h2cone.annotation.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({
        ElementType.TYPE,
        ElementType.METHOD
})
public @interface Inspect {

    boolean ignore() default false;

}
