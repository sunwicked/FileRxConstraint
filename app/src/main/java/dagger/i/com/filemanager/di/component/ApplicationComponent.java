package dagger.i.com.filemanager.di.component;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;
import dagger.i.com.filemanager.FileApplication;
import dagger.i.com.filemanager.data.DataManager;
import dagger.i.com.filemanager.data.SharedPrefsHelper;
import dagger.i.com.filemanager.di.ApplicationContext;
import dagger.i.com.filemanager.di.module.ApplicationModule;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(FileApplication demoApplication);

    @ApplicationContext
    Context getContext();

    Application getApplication();

    DataManager getDataManager();

    SharedPrefsHelper getPreferenceHelper();

}