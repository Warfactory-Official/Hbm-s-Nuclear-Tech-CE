package com.hbm.render.chunk;

/** Implemented on every tile entity; written on the client thread, read by compile workers. */
public interface ISectionGeometryTile {

    boolean hbm$globalRender();

    void hbm$globalRender(boolean global);
}
