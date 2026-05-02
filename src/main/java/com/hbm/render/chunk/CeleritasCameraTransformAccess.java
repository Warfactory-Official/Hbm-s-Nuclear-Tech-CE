package com.hbm.render.chunk;

import com.hbm.lib.internal.UnsafeHolder;
import org.embeddedt.embeddium.impl.render.viewport.CameraTransform;

import static com.hbm.lib.internal.UnsafeHolder.U;

/**
 * Shitty workaround that accesses {@link CameraTransform} through raw field offsets instead of normal {@code getfield}.
 *
 * <p>User reported sections being incorrectly culled with Celeritas present. After hours of exhaustive search I have
 * determined that I did EVERYTHING RIGHT. I instrumented every possible failure path, and came to a conclusion that:
 * {@code Frustum.sodium$createViewport()} computed the correct nonzero camera position, {@code Viewport.<init>}
 * received that correct position, and {@code CameraTransform.<init>} received the correct constructor arguments too.
 * However, ordinary field reads from the resulting {@code CameraTransform} instance still observed
 * {@code x/y/z == 0}, {@code intX/intY/intZ == 0}, and {@code fracX/fracY/fracZ == 0} in affected sections.
 *
 * <p>That contradiction was the key clue. Raw {@link sun.misc.Unsafe} writes and reads could observe and repair the expected
 * backing values, but downstream Celeritas code paths using normal field access still behaved as if the transform were
 * zeroed unless those call sites were redirected as well. In other words, fixing the constructor output alone was not
 * sufficient; the terrain visibility/render path had to consume the transform through raw offset reads.
 *
 * <p>This helper centralizes those raw reads so the compat mixins can redirect only the affected Celeritas call sites
 * without duplicating offset logic everywhere. If Celeritas (or a future Java release, since I don't know what is the
 * real culprit is) fixes the underlying {@code CameraTransform} field-access breakage upstream, this class and its
 * associated redirects should be removable.
 */
public final class CeleritasCameraTransformAccess {

    public static final long INT_X_OFFSET = UnsafeHolder.fieldOffset(CameraTransform.class, "intX");
    public static final long INT_Y_OFFSET = UnsafeHolder.fieldOffset(CameraTransform.class, "intY");
    public static final long INT_Z_OFFSET = UnsafeHolder.fieldOffset(CameraTransform.class, "intZ");
    public static final long FRAC_X_OFFSET = UnsafeHolder.fieldOffset(CameraTransform.class, "fracX");
    public static final long FRAC_Y_OFFSET = UnsafeHolder.fieldOffset(CameraTransform.class, "fracY");
    public static final long FRAC_Z_OFFSET = UnsafeHolder.fieldOffset(CameraTransform.class, "fracZ");
    public static final long X_OFFSET = UnsafeHolder.fieldOffset(CameraTransform.class, "x");
    public static final long Y_OFFSET = UnsafeHolder.fieldOffset(CameraTransform.class, "y");
    public static final long Z_OFFSET = UnsafeHolder.fieldOffset(CameraTransform.class, "z");

    private CeleritasCameraTransformAccess() {
    }

    public static int getIntX(CameraTransform transform) {
        return U.getInt(transform, INT_X_OFFSET);
    }

    public static int getIntY(CameraTransform transform) {
        return U.getInt(transform, INT_Y_OFFSET);
    }

    public static int getIntZ(CameraTransform transform) {
        return U.getInt(transform, INT_Z_OFFSET);
    }

    public static float getFracX(CameraTransform transform) {
        return U.getFloat(transform, FRAC_X_OFFSET);
    }

    public static float getFracY(CameraTransform transform) {
        return U.getFloat(transform, FRAC_Y_OFFSET);
    }

    public static float getFracZ(CameraTransform transform) {
        return U.getFloat(transform, FRAC_Z_OFFSET);
    }

    public static double getX(CameraTransform transform) {
        return U.getDouble(transform, X_OFFSET);
    }

    public static double getY(CameraTransform transform) {
        return U.getDouble(transform, Y_OFFSET);
    }

    public static double getZ(CameraTransform transform) {
        return U.getDouble(transform, Z_OFFSET);
    }
}
