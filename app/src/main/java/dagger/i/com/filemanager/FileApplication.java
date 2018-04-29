package dagger.i.com.filemanager;

import android.app.Application;
import android.content.Context;

import javax.inject.Inject;

import dagger.i.com.filemanager.data.DataManager;
import dagger.i.com.filemanager.di.component.ApplicationComponent;
import dagger.i.com.filemanager.di.component.DaggerApplicationComponent;
import dagger.i.com.filemanager.di.module.ApplicationModule;

public class FileApplication extends Application {


    protected ApplicationComponent applicationComponent;

    @Inject
    DataManager dataManager;


    public static FileApplication get(Context context) {
        return (FileApplication) context.getApplicationContext();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        applicationComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        applicationComponent.inject(this);
    }

    public ApplicationComponent getComponent(){
        return applicationComponent;
    }
}
