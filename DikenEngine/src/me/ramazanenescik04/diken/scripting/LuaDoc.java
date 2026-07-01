package me.ramazanenescik04.diken.scripting;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
public @interface LuaDoc {
    String description() default "";
    String returns() default "";
    String[] params() default {};
    String example() default "";
}