package me.hauvo.thumbnail;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;


public class RNThumbnailModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public RNThumbnailModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNThumbnail";
    }

    @ReactMethod
    public void get(String filePath, Promise promise) {
        filePath = filePath.replace("file://", "");
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        Bitmap image = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

        String fullPath = this.reactContext.getCacheDir().getAbsolutePath() + "/thumb";

        try {
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            OutputStream fOut = null;
            // String fileName = "thumb-" + UUID.randomUUID().toString() + ".jpeg";
            String fileName = "thumb-" + UUID.randomUUID().toString() + ".jpeg";
            File file = new File(fullPath, fileName);
            file.createNewFile();
            fOut = new FileOutputStream(file);

            // 100 means no compression, the lower you go, the stronger the compression
            image.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
            fOut.flush();
            fOut.close();

            // MediaStore.Images.Media.insertImage(reactContext.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

            WritableMap map = Arguments.createMap();
            map.putString("path", "file://" + fullPath + '/' + fileName);
            map.putDouble("width", image.getWidth());
            map.putDouble("height", image.getHeight());

            promise.resolve(map);

        } catch (Exception e) {
            Log.e("E_RNThumnail_ERROR", e.getMessage());
            promise.reject("E_RNThumnail_ERROR", e);
        }
    }
}
