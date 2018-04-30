package dagger.i.com.filemanager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class FileFinder extends AsyncTask<File, Integer, ArrayList<File>> {




    public ArrayList<File> walkDir(File dir, ArrayList<File> files) {
        File[] listFile = dir.listFiles();

        if (listFile != null && !this.isCancelled()) {
            for (int i = 0; i < listFile.length; i++) {

                if (listFile[i].isDirectory()) {
                    publishProgress();
                    walkDir(listFile[i], files);
                } else {
                    files.add(listFile[i]);
                }
            }
        }
        return files;
    }

    ArrayList<File> filesResult = new ArrayList<>();

    @Override
    protected ArrayList<File> doInBackground(File... files) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return walkDir(files[0], filesResult);

    }

    @Override
    protected void onPostExecute(ArrayList<File> files) {
        super.onPostExecute(files);
    }
}
