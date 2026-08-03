package com.mobdeve.s17.grp2.archerfind;

import android.net.Uri;

// Builds a Google Static Maps API image URL for a pinned location. Used instead of
// embedding the full Maps SDK (which needs an interactive MapView + more setup) —
// a plain image loaded through Glide still visually satisfies "display and pin the
// location on a map" without the extra integration surface.
public final class StaticMapUtil {

    private StaticMapUtil() {
    }

    public static boolean isConfigured() {
        return BuildConfig.MAPS_API_KEY != null && !BuildConfig.MAPS_API_KEY.isEmpty();
    }

    public static String buildUrl(double latitude, double longitude) {
        String location = latitude + "," + longitude;
        return Uri.parse("https://maps.googleapis.com/maps/api/staticmap").buildUpon()
                .appendQueryParameter("center", location)
                .appendQueryParameter("zoom", "16")
                .appendQueryParameter("size", "600x300")
                .appendQueryParameter("scale", "2")
                .appendQueryParameter("markers", "color:red|" + location)
                .appendQueryParameter("key", BuildConfig.MAPS_API_KEY)
                .build()
                .toString();
    }
}
