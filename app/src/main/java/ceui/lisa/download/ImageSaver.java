package ceui.lisa.download;

import android.media.MediaScannerConnection;
import android.net.Uri;

import java.io.File;

import ceui.lisa.activities.Shaft;

public abstract class ImageSaver {

    abstract File whichFile();

    void execute(){
        File file = whichFile();
        if(file == null){
            return;
        }

        String[] path = new String[1];
        String[] mime = new String[1];
        path[0] = file.getPath();
        mime[0] = "image/png";
        MediaScannerConnection.scanFile(Shaft.getContext()
                , path, mime, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {

                    }
                });
    }
}