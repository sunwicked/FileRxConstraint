package dagger.i.com.filemanager.data;

import android.content.Context;

import javax.inject.Inject;

import dagger.i.com.filemanager.di.ApplicationContext;

public class DataManager {

    private Context mContext;
    private SharedPrefsHelper mSharedPrefsHelper;

    @Inject
    public DataManager(@ApplicationContext Context context,
                       SharedPrefsHelper sharedPrefsHelper) {
        mContext = context;
        mSharedPrefsHelper = sharedPrefsHelper;
    }

    public void saveString(String accessToken, String key) {
        mSharedPrefsHelper.put(key, accessToken);
    }

    public String getString(String key){
        return mSharedPrefsHelper.get(key, null);
    }

}
