package com.hbm.render.chunk;

/** Implemented on every block state; {@code true} => terrain draws nothing and {@link SectionGeometry} owns the model. */
public interface ISectionGeometryState {

    ThreadLocal<Boolean> FOREIGN_MESHER = ThreadLocal.withInitial(() -> false);

    boolean hbm$sectioned();

    void hbm$sectioned(boolean sectioned);
}
