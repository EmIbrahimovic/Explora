/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.personal.project.explora;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.personal.project.explora.db.EpisodeDatabase;
import com.personal.project.explora.service.PlayerServiceConnection;

/**
 * Android Application class. Used for accessing singletons.
 */
public class BasicApp extends Application {

    private final AppExecutors mAppExecutors;

    SharedPreferences sharedPreferences;

    public BasicApp() {
        mAppExecutors = new AppExecutors();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public AppExecutors getAppExecutors() {
        return mAppExecutors;
    }

    public EpisodeDatabase getDatabase() {
        /*if(AppStartUtil.checkAppStart(this, sharedPreferences) == AppStartUtil.AppStart.NORMAL)
            return EpisodeDatabase.getInstance(this);*/

        return EpisodeDatabase.getInstance(this);
    }

    public EpisodeRepository getRepository() {
        return EpisodeRepository.getInstance(this, mAppExecutors);
    }

    public PlayerServiceConnection getPlayerServiceConnection() {
        return PlayerServiceConnection.getInstance(this);
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

}
