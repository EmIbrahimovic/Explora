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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.personal.project.explora.db.EpisodeDatabase;
import com.personal.project.explora.utils.AppStartUtil;

/**
 * Android Application class. Used for accessing singletons.
 */
public class BasicApp extends Application {

    private AppExecutors mAppExecutors;

    SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppExecutors = new AppExecutors();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public EpisodeDatabase getDatabase() {
        if(AppStartUtil.checkAppStart(this, sharedPreferences) == AppStartUtil.AppStart.FIRST_TIME_VERSION)
            return EpisodeDatabase.getInstanceFromAsset(this);

        return EpisodeDatabase.getInstance(this);
    }

    public EpisodeRepository getRepository() {
        return EpisodeRepository.getInstance(this, mAppExecutors);
    }

}
