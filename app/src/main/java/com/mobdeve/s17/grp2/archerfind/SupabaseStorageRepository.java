package com.mobdeve.s17.grp2.archerfind;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseStorageRepository {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static String extensionFor(String mimeType) {
        if ("image/png".equals(mimeType)) return ".png";
        if ("image/webp".equals(mimeType)) return ".webp";
        return ".jpg";
    }

    // Uploads to the public bucket configured via local.properties and returns its public URL.
    public void uploadItemPhoto(byte[] imageBytes, String mimeType, FirestoreCallback<String> callback) {
        String supabaseUrl = BuildConfig.SUPABASE_URL;
        String anonKey = BuildConfig.SUPABASE_ANON_KEY;
        String bucket = BuildConfig.SUPABASE_BUCKET;

        if (supabaseUrl.isEmpty() || anonKey.isEmpty()) {
            mainHandler.post(() -> callback.onError(new IllegalStateException(
                    "Supabase is not configured. Add SUPABASE_URL/SUPABASE_ANON_KEY to local.properties.")));
            return;
        }

        String effectiveMimeType = mimeType != null ? mimeType : "image/jpeg";
        String path = "items/" + UUID.randomUUID() + extensionFor(effectiveMimeType);
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + path;
        String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;

        RequestBody body = RequestBody.create(imageBytes, MediaType.parse(effectiveMimeType));
        Request request = new Request.Builder()
                .url(uploadUrl)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer " + anonKey)
                .addHeader("Content-Type", effectiveMimeType)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (response.isSuccessful()) {
                        mainHandler.post(() -> callback.onSuccess(publicUrl));
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : response.message();
                        mainHandler.post(() -> callback.onError(new IOException("Upload failed: " + errorBody)));
                    }
                } catch (IOException e) {
                    mainHandler.post(() -> callback.onError(e));
                } finally {
                    response.close();
                }
            }
        });
    }
}
