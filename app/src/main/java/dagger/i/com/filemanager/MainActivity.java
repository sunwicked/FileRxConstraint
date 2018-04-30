package dagger.i.com.filemanager;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.i.com.FileAdapter;

import static dagger.i.com.filemanager.FileUtils.calculateAvgFileSize;
import static dagger.i.com.filemanager.FileUtils.generateFileAnalytics;
import static dagger.i.com.filemanager.FileUtils.getFrequency;
import static dagger.i.com.filemanager.Permissions.AndroidRuntimePermission;
import static dagger.i.com.filemanager.Permissions.RUNTIME_PERMISSION_CODE;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn)
    Button btnScan;

    @BindView(R.id.rv_files)
    RecyclerView rvFiles;
    @BindView(R.id.avgFileSize)
    TextView avgFileSize;
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.textView2)
    TextView textView2;
    @BindView(R.id.textView3)
    TextView textView3;
    @BindView(R.id.textView4)
    TextView textView4;
    @BindView(R.id.textView5)
    TextView textView5;
    @BindView(R.id.textView1Stats)
    TextView textView1Stats;
    @BindView(R.id.textView2Stats)
    TextView textView2Stats;
    @BindView(R.id.textView3Stats)
    TextView textView3Stats;
    @BindView(R.id.textView4Stats)
    TextView textView4Stats;
    @BindView(R.id.textView5Stats)
    TextView textView5Stats;
    private FileFinder finder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AndroidRuntimePermission(this);
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            btnScan.setEnabled(false);
        }

    }


    @OnClick(R.id.btn)
    public void Scan(View view) {
        // scan start stop
        ArrayList<File> files = new ArrayList<>();
        if (finder != null) {
            finder.cancel(true);
        }
        finder = new FileFinder();

        NotificationHandler.setNotification("Scanning", "File Scan is in progress", this);
        try {
            //simulating delay in some cases loading is very fast
            files = finder.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, Environment.getExternalStorageDirectory()).get();
            ArrayList<File> topFiles = generateFileAnalytics(files);
            setAdapter(topFiles);
            avgFileSize.setText(calculateAvgFileSize(files));
            setFrequentFilesTypes(files);
            NotificationHandler.cancelNotification(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void setFrequentFilesTypes(ArrayList<File> files) {
        List<FileModel> fileModels = getFrequency(files);
        textView.setText(fileModels.get(0).getName());
        textView1Stats.setText(fileModels.get(0).getFreq());
        textView2.setText(fileModels.get(1).getName());
        textView2Stats.setText(fileModels.get(1).getFreq());
        textView3.setText(fileModels.get(2).getName());
        textView3Stats.setText(fileModels.get(2).getFreq());
        textView4.setText(fileModels.get(3).getName());
        textView4Stats.setText(fileModels.get(3).getFreq());
        textView5.setText(fileModels.get(4).getName());
        textView5Stats.setText(fileModels.get(4).getFreq());


    }


    private void setAdapter(ArrayList<File> files) {
        rvFiles.setAdapter(new FileAdapter(files));
        rvFiles.setLayoutManager(new LinearLayoutManager(this));
        rvFiles.setItemAnimator(new DefaultItemAnimator());
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finder.cancel(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case RUNTIME_PERMISSION_CODE: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
            }
        }
    }

}
