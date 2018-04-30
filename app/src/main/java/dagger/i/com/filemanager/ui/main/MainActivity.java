package dagger.i.com.filemanager.ui.main;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.i.com.filemanager.FileApplication;
import dagger.i.com.filemanager.FileModel;
import dagger.i.com.filemanager.NotificationHandler;
import dagger.i.com.filemanager.R;
import dagger.i.com.filemanager.di.component.ActivityComponent;
import dagger.i.com.filemanager.di.component.DaggerActivityComponent;
import dagger.i.com.filemanager.di.module.ActivityModule;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static dagger.i.com.filemanager.FileUtils.calculateAvgFileSize;
import static dagger.i.com.filemanager.FileUtils.generateFileAnalytics;
import static dagger.i.com.filemanager.FileUtils.getFrequency;
import static dagger.i.com.filemanager.FileUtils.walkDir;
import static dagger.i.com.filemanager.Permissions.AndroidRuntimePermission;
import static dagger.i.com.filemanager.Permissions.RUNTIME_PERMISSION_CODE;
import static dagger.i.com.filemanager.ViewUtils.toggleVisibility;

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
    ProgressDialog progressDialog;
    @BindView(R.id.iv_share)
    ImageView ivShare;
    @BindView(R.id.textView11)
    TextView textView11;
    @BindView(R.id.textView6)
    TextView tvHintText;
    private ArrayList<File> files;

    private ActivityComponent activityComponent;
    private boolean isCancelled = false;
    ArrayList<TextView> viewList = new ArrayList<>();
    private boolean setOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getActivityComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        buildViewList();
        toggleVisibility(viewList, false);
        AndroidRuntimePermission(this);
        initProgressDialog();
    }


    private void buildViewList() {
        viewList.add(textView);
        viewList.add(textView1Stats);
        viewList.add(textView2);
        viewList.add(textView2Stats);
        viewList.add(textView3);
        viewList.add(textView3Stats);
        viewList.add(textView4);
        viewList.add(textView4Stats);
        viewList.add(textView5);
        viewList.add(textView5Stats);
        viewList.add(avgFileSize);
        viewList.add(textView11);
    }

    void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.loading_msg));
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
             cancelScanProcess();
            }
        });
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "STOP", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelScanProcess();
            }
        });
    }

    private void cancelScanProcess() {
        progressDialog.dismiss();
        initiateCancellation();
        if (files.isEmpty() && !setOnce)
            tvHintText.setVisibility(View.VISIBLE);
        isCancelled = false;
    }

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @OnClick(R.id.btn)
    public void scan(View view) {
        // scan start stop
        files = new ArrayList<>();
        tvHintText.setVisibility(View.INVISIBLE);
        ivShare.setVisibility(View.INVISIBLE);
        NotificationHandler.setNotification("Scanning", "File scan is in progress", this);
        progressDialog.show();

        buildRxDisposable();
    }

    private void buildRxDisposable() {
        Disposable disposable = fileObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<File>>() {
                    @Override
                    public void onNext(ArrayList<File> files) {

                        if (!files.isEmpty()) {
                            setValues(files);
                        } else {
                            cancelScanProcess();
                            Snackbar.make(tvHintText, "Retry Scanning", Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        compositeDisposable.add(disposable);
    }


    public ActivityComponent getActivityComponent() {
        if (activityComponent == null) {
            activityComponent = DaggerActivityComponent.builder()
                    .activityModule(new ActivityModule(this))
                    .applicationComponent(FileApplication.get(this).getComponent())
                    .build();
        }
        return activityComponent;
    }


    Observable<ArrayList<File>> fileObservable = Observable.fromCallable(new Callable<ArrayList<File>>() {
        @Override
        public ArrayList<File> call() throws Exception {
            try {
                Thread.sleep(2000); // simulating delay to show progress loading
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!isCancelled)
                return walkDir(Environment.getExternalStorageDirectory(), files);
            else
                return files;
        }
    });


    @OnClick(R.id.iv_share)
    public void share(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.total_files_are) + files.size());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void setValues(ArrayList<File> files) {
        ArrayList<File> topFiles = generateFileAnalytics(files);
        setAdapter(topFiles);
        avgFileSize.setText(calculateAvgFileSize(files));
        setFrequentFilesTypes(files);
        NotificationHandler.cancelNotification(this);
        progressDialog.cancel();
        ivShare.setVisibility(View.VISIBLE);
        setOnce = true;
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
        toggleVisibility(viewList, true);

    }


    private void setAdapter(ArrayList<File> files) {
        rvFiles.setAdapter(new FileAdapter(files));
        rvFiles.setLayoutManager(new LinearLayoutManager(this));
        rvFiles.setItemAnimator(new DefaultItemAnimator());
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
        initiateCancellation();
    }

    private void initiateCancellation() {
        isCancelled = true;
        NotificationHandler.cancelNotification(this);
        compositeDisposable.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        initiateCancellation();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        switch (requestCode) {

            case RUNTIME_PERMISSION_CODE: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    finish();
                }
            }
        }
    }


}
