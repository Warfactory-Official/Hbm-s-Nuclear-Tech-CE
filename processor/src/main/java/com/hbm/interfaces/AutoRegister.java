/*
 * Copyright (c) 2025-2026 movblock. All Rights Reserved.
 *
 * This program is NOT under the repo GPL/LGPL. See processor/LICENSE.
 */
package com.hbm.interfaces;

import java.lang.annotation.*;

/**
 * A versatile annotation that automates the registration of nearly everything.
 * <p>
 * This annotation is processed at compile time to generate the necessary registration
 * boilerplate code. It can be applied to entities, tile entities, and renderer classes.
 * The behavior of the annotation changes based on the type of class it is applied to.
 * <ul>
 *   <li><b>Entity Renderer:</b> When annotating a class that extends {@code Render<E>},
 *       use {@link #entity()} to specify the target entity class. If omitted, the processor will attempt
 *       to infer the entity type from the renderer's generic parameter. If the factory method requires a specific
 *       {@code IRenderFactory}, specify its name in {@link #factory()}.</li>
 *
 *   <li><b>Item Stack Renderer:</b> When annotating a class that extends
 *       {@code TileEntityItemStackRenderer},
 *       use {@link #item()} to specify the static field name of the {@code Item} this renderer should be bound to.</li>
 *       use {@link #constructorArgs()} to provide constructor arguments.
 *
 *   <li><b>Tile Entity Renderer:</b> When annotating a class that extends {@code TileEntitySpecialRenderer<T>},
 *       use {@link #tileentity()} to specify the target tile entity class. If omitted, the processor will
 *       attempt to infer the tile entity type from the renderer's generic parameter.</li>
 *
 *   <li><b>Entity:</b> When annotating a class that extends {@code Entity},
 *       the {@link #name()} field is mandatory. Use {@link #trackingRange()}, {@link #updateFrequency()},
 *       and {@link #sendVelocityUpdates()} to configure its behavior.</li>
 *
 *   <li><b>Tile Entity:</b> When annotating a class that extends {@code TileEntity},
 *       use {@link #name()} to provide a custom registration name. If the name is omitted, one will be
 *       generated automatically from the class name (e.g., {@code TileEntityAwesome} becomes {@code "tileentity_awesome"}).</li>
 * </ul>
 *
 * @see com.hbm.processor.AutoRegisterProcessor The annotation processor that handles this annotation.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Repeatable(AutoRegisterContainer.class)
public @interface AutoRegister {

    /**
     * Specifies the entity class that this renderer should be bound to. Optional.
     * <p>
     * This is applicable when the annotated class is an entity renderer
     * (i.e., extends {@code Render<E>}).
     * If this is not set, the processor will attempt to infer the entity type from the
     * renderer's generic type parameter.
     */
    Class<?> entity() default Object.class;

    /**
     * Specifies the name of the static {@code Item} field to which this
     * {@code TileEntityItemStackRenderer} should be bound.
     * <p>
     * For example, a value of {@code "hf_sword"} would target a field like {@code ModItems.hf_sword}.
     * This is required when annotating a TEISR class. Use {@link #constructorArgs()} to provide
     * constructor arguments.
     */
    String item() default "";

    /**
     * Specifies the tile entity class that this special renderer should be bound to.
     * Optional.
     * <p>
     * This is applicable when the annotated class is a tile entity renderer
     * (i.e., extends {@code TileEntitySpecialRenderer}).
     * If this is not set, the processor will attempt to infer the tile entity type from the
     * renderer's generic type parameter.
     */
    Class<?> tileentity() default Object.class;

    /**
     * Specifies the name of the static {@code IRenderFactory} field to be used for
     * instantiating an entity renderer. Optional.
     * <p>
     * The field must be a {@code public static final} member of the annotated renderer class.
     * If this is not set, the processor will use {@code AnnotatedClass::new}.
     */
    String factory() default "";

    /**
     * The unique registration name for an entity or tile entity.
     * <p>
     * For entities, this field is <strong>mandatory</strong> and must not be empty.
     * <p>
     * For tile entities, this field is optional. If left empty, a name will be
     * automatically generated from the tile entity's class name.
     */
    String name() default "";

    /**
     * The range at which MC will send tracking updates. Optional.
     * <p>
     * Only applicable when annotating an entity class. Default = 250.
     */
    int trackingRange() default 250;

    /**
     * The frequency of tracking updates. Optional.
     * <p>
     * Only applicable when annotating an entity class. Default = 1.
     */
    int updateFrequency() default 1;

    /**
     * Whether to send velocity information packets. Optional.
     * <p>
     * Only applicable when annotating an entity class. Default = true.
     */
    boolean sendVelocityUpdates() default true;

    /**
     * Specifies raw string literals to be used as constructor arguments for {@code TileEntityItemStackRenderer}. Optional.
     * <p>
     * Each string in the array is injected directly as a parameter into the constructor call.
     * <ul>
     *   <li><b>Floats:</b> {@code constructorArgs = {"1.0F", "0.5F"}}</li>
     *   <li><b>Integers (Hex):</b> {@code constructorArgs = {"0x575757", "0.2F"}}</li>
     *   <li><b>Strings:</b> {@code constructorArgs = {"\"my_texture.png\""}} (Note the escaped quotes)</li>
     * </ul>
     * <b> An alternative to this is {@link #constructorArgsString()}
     */
    String[] constructorArgs() default {};

    /**
     * A convenience alternative to {@link #constructorArgs} that accepts a single, comma-separated string.
     * <p>
     * This is useful for simple argument lists. The processor will use this string directly as the
     * content within the constructor's parentheses.
     * <p>
     * <b>Example:</b>
     * <pre>
     * {@code @AutoRegister(item = "...", constructorArgsString = "0.5F, 0.5F, 1.0F")}
     * </pre>
     *
     * <b>You should use either this member or {@code constructorArgs}, but not both.</b>
     *
     * @see #constructorArgs()
     */
    String constructorArgsString() default "";

    /**
     * For TEISR: name of a public static field on the renderer class to use instead of calling its constructor,
     * e.g. "INSTANCE". If empty, a new instance will be created using constructor args.
     */
    String instanceField() default "";

    /**
     * This parameter is used only for mob entities to recolor the egg.
     * The primary and secondary colors are used.
     */

    int[] eggColors() default {0, 0};
}
