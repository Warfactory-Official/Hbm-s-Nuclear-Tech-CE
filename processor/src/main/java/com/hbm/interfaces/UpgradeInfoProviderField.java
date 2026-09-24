/*
 * Copyright (c) 2026 movblock. All Rights Reserved.
 *
 * This program is NOT under the repo GPL/LGPL. See processor/LICENSE.
 */
package com.hbm.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Explicitly selects the field a GUI should expose as its upgrade info provider source when the
 * compiler plugin cannot infer it unambiguously.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface UpgradeInfoProviderField {
    String value();
}
