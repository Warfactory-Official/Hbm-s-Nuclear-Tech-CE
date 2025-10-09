package com.hbm.interfaces;

import java.lang.annotation.*;

/**
 * Historically, NTM has had so many comments that are either funny or lengthy rants or other silly shit that it's
 * hard to keep track of all of it, this annotation shall be used on classes with noteworthy comments.
 *
 * @author hbm
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface NotableComments {
}
