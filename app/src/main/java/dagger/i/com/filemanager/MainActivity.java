package dagger.i.com.filemanager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.i.com.FileAdapter;

import static dagger.i.com.filemanager.FileUtils.calculateAvgFileSize;
import static dagger.i.com.filemanager.FileUtils.generateFileAnalytics;
import static dagger.i.com.filemanager.FileUtils.getFrequency;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn)
    Button btnScan;

    public static final int RUNTIME_PERMISSION_CODE = 7;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            btnScan.setEnabled(false);
        }
        AndroidRuntimePermission();

    }


    @OnClick(R.id.btn)
    public void Scan(View view) {
        // scan start stop
        ArrayList<File> files = getFile(Environment.getExternalStorageDirectory());
        ArrayList<File> topFiles = generateFileAnalytics(files);
        setAdapter(topFiles);
        avgFileSize.setText(calculateAvgFileSize(files));
        setFrequentFilesTypes(files);

    }

    private void setFrequentFilesTypes(ArrayList<File> files) {
        List<FileModel> fileModels= getFrequency(files);
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


    Cursor cursor;

    Uri uri;

    public ArrayList<File> getFile(File root) {
        ContentResolver contentResolver = this.getContentResolver();
         ArrayList<File> fileList = new ArrayList<>();
        uri = MediaStore.Files
                .getContentUri("external");
        final String[] projection = {MediaStore.Files.FileColumns.DATA};
        cursor = contentResolver.query(
                uri, // Uri
                projection,
                null,
                null,
                null
        );

        if (cursor == null) {

            Toast.makeText(MainActivity.this, "Something Went Wrong.", Toast.LENGTH_LONG);

        } else if (!cursor.moveToFirst()) {

            Toast.makeText(MainActivity.this, "No Files Found on SD Card.", Toast.LENGTH_LONG);

        } else {


            do {

                String path = cursor.getString(cursor.getColumnIndex
                        (MediaStore.Files.FileColumns.DATA));
                File f = new File(path);
                fileList.add(f);

            } while (cursor.moveToNext());
        }

        return fileList;
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

    // Creating Runtime permission function.
    public void AndroidRuntimePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    AlertDialog.Builder alert_builder = new AlertDialog.Builder(MainActivity.this);
                    alert_builder.setMessage("External Storage Permission is Required.");
                    alert_builder.setTitle("Please Grant Permission.");
                    alert_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    RUNTIME_PERMISSION_CODE

                            );
                        }
                    });

                    alert_builder.setNeutralButton("Cancel", null);

                    AlertDialog dialog = alert_builder.create();

                    dialog.show();

                } else {

                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            RUNTIME_PERMISSION_CODE
                    );
                }
            } else {

            }
        }
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
