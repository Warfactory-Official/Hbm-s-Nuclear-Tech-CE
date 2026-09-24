package com.hbm.render.chunk;

import org.jetbrains.annotations.Nullable;

/** Implemented on compile tasks; set on the client thread when the task is created. */
public interface ISectionGeometryHolder {

    SectionGeometry.@Nullable Snapshot hbm$sectionGeometry();

    void hbm$sectionGeometry(SectionGeometry.@Nullable Snapshot snapshot);
}
