/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.liguang.imageloaderdemo.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.liguang.imageloaderdemo.data.ItemsDataSourceRxJava;
import com.liguang.imageloaderdemo.data.ItemsRepositoryRxJava;
import com.liguang.imageloaderdemo.data.local.ItemsLocalDataSourceRxJava;
import com.liguang.imageloaderdemo.data.remote.ItemsRemoteDataSourceRxJava;

import static com.facebook.common.internal.Preconditions.checkNotNull;

public class Injection {

    public static ItemsRepositoryRxJava provideTasksRepository(Context context) {
        return ItemsRepositoryRxJava.getInstance(provideRemoteDataSource(), provideLocalDataSource(context));
    }

    public static ItemsDataSourceRxJava provideRemoteDataSource() {
        return ItemsRemoteDataSourceRxJava.getInstance();
    }

    public static ItemsDataSourceRxJava provideLocalDataSource(@NonNull Context context) {
        checkNotNull(context);
        return ItemsLocalDataSourceRxJava.getInstance(context.getContentResolver());
    }

}
