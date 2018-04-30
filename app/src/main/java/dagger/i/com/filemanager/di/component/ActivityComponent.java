package dagger.i.com.filemanager.di.component;

import dagger.Component;
import dagger.i.com.filemanager.ui.main.MainActivity;
import dagger.i.com.filemanager.di.PerActivity;
import dagger.i.com.filemanager.di.module.ActivityModule;

@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(MainActivity mainActivity);

}