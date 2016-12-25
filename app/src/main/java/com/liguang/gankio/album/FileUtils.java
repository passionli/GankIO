package com.liguang.gankio.album;

import android.os.Environment;
import android.util.Log;

import com.liguang.gankio.album.model.FileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    private static final String TAG = "FileUtils";

    public static List<ArrayList<FileItem>> load(String path) {
        List<ArrayList<FileItem>> result = new ArrayList<ArrayList<FileItem>>();
        String root = Environment.getExternalStorageDirectory() + File.separator + path;
        File folder = new File(root);
        ArrayList<FileItem> arrayList = new ArrayList<>();
        for (File file : folder.listFiles()) {
            Log.d(TAG, "load: " + file.toString());
            if (file.getName().endsWith(".jpg")) {
                FileItem fileItem = new FileItem("file:" + root + File.separator + file.getName(), "asdfadsf");
                arrayList.add(fileItem);
            }
//            for (File f : file.listFiles()) {
//            }
        }
        result.add(arrayList);
        return result;
    }
}
