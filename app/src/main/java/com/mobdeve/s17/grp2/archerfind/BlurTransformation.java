package com.mobdeve.s17.grp2.archerfind;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

// A cheap pixelation-style blur (downscale then upscale) used to obscure item
// photos from non-owners as a false-claim deterrent. Deliberately avoids
// RenderScript (deprecated, being removed from newer Android versions) so it
// needs no platform blur API at all.
public class BlurTransformation extends BitmapTransformation {

    private static final String ID = "com.mobdeve.s17.grp2.archerfind.BlurTransformation";
    private static final byte[] ID_BYTES = ID.getBytes(StandardCharsets.UTF_8);
    private static final int SCALE_FACTOR = 16;

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        int width = toTransform.getWidth();
        int height = toTransform.getHeight();
        int scaledWidth = Math.max(1, width / SCALE_FACTOR);
        int scaledHeight = Math.max(1, height / SCALE_FACTOR);

        Bitmap scaledDown = Bitmap.createScaledBitmap(toTransform, scaledWidth, scaledHeight, true);
        return Bitmap.createScaledBitmap(scaledDown, width, height, true);
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BlurTransformation;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }
}
