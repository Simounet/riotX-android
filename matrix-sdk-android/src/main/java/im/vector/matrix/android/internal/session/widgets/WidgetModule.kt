/*
 * Copyright (c) 2020 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.matrix.android.internal.session.widgets

import dagger.Binds
import dagger.Module
import dagger.Provides
import im.vector.matrix.android.api.session.widgets.WidgetPostAPIMediator
import im.vector.matrix.android.api.session.widgets.WidgetService
import im.vector.matrix.android.internal.session.widgets.token.DefaultGetScalarTokenTask
import im.vector.matrix.android.internal.session.widgets.token.GetScalarTokenTask
import retrofit2.Retrofit

@Module
internal abstract class WidgetModule {

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun providesWidgetsAPI(retrofit: Retrofit): WidgetsAPI {
            return retrofit.create(WidgetsAPI::class.java)
        }
    }

    @Binds
    abstract fun bindWidgetService(widgetService: DefaultWidgetService): WidgetService

    @Binds
    abstract fun bindWidgetPostAPIMediator(widgetPostMessageAPIProvider: DefaultWidgetPostAPIMediator): WidgetPostAPIMediator

    @Binds
    abstract fun bindCreateWidgetTask(task: DefaultCreateWidgetTask): CreateWidgetTask

    @Binds
    abstract fun bindGetScalarTokenTask(task: DefaultGetScalarTokenTask): GetScalarTokenTask
}