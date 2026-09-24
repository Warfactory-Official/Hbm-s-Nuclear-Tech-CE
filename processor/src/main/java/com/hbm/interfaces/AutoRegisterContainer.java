/*
 * Copyright (c) 2025-2026 movblock. All Rights Reserved.
 *
 * This program is NOT under the repo GPL/LGPL. See processor/LICENSE.
 */
package com.hbm.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface AutoRegisterContainer {
    AutoRegister[] value();
}
