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

package im.vector.matrix.android.internal.session.widgets.token

import im.vector.matrix.android.api.failure.Failure
import im.vector.matrix.android.internal.network.executeRequest
import im.vector.matrix.android.internal.session.openid.GetOpenIdTokenTask
import im.vector.matrix.android.internal.session.widgets.RegisterWidgetResponse
import im.vector.matrix.android.internal.session.widgets.WidgetsAPI
import im.vector.matrix.android.internal.session.widgets.WidgetsAPIProvider
import im.vector.matrix.android.internal.task.Task
import java.net.HttpURLConnection
import javax.inject.Inject

internal interface GetScalarTokenTask : Task<GetScalarTokenTask.Params, String> {

    data class Params(
            val serverUrl: String
    )
}

private const val WIDGET_API_VERSION = "1.1"

internal class DefaultGetScalarTokenTask @Inject constructor(private val widgetsAPIProvider: WidgetsAPIProvider,
                                                             private val scalarTokenStore: ScalarTokenStore,
                                                             private val getOpenIdTokenTask: GetOpenIdTokenTask) : GetScalarTokenTask {

    override suspend fun execute(params: GetScalarTokenTask.Params): String {
        val widgetsAPI = widgetsAPIProvider.get(params.serverUrl)
        val scalarToken = scalarTokenStore.getToken(params.serverUrl)
        return if (scalarToken == null) {
            getNewScalarToken(widgetsAPI, params.serverUrl)
        } else {
            validateToken(widgetsAPI, params.serverUrl, scalarToken)
        }
    }

    private suspend fun getNewScalarToken(widgetsAPI: WidgetsAPI, serverUrl: String): String {
        val openId = getOpenIdTokenTask.execute(Unit)
        val registerWidgetResponse = executeRequest<RegisterWidgetResponse>(null) {
            apiCall = widgetsAPI.register(openId, WIDGET_API_VERSION)
        }
        if (registerWidgetResponse.scalarToken == null) {
            // Should not happen
            throw IllegalStateException("Scalar token is null")
        }
        scalarTokenStore.setToken(serverUrl, registerWidgetResponse.scalarToken)
        widgetsAPI.validateToken(registerWidgetResponse.scalarToken, WIDGET_API_VERSION)
        return registerWidgetResponse.scalarToken
    }

    private suspend fun validateToken(widgetsAPI: WidgetsAPI, serverUrl: String, scalarToken: String): String {
        return try {
            widgetsAPI.validateToken(scalarToken, WIDGET_API_VERSION)
            scalarToken
        } catch (failure: Throwable) {
            if (failure.isScalarTokenError()) {
                scalarTokenStore.clearToken(serverUrl)
                getNewScalarToken(widgetsAPI, serverUrl)
            } else {
                throw failure
            }
        }
    }

    private fun Throwable.isScalarTokenError(): Boolean {
        return this is Failure.ServerError && this.httpCode == HttpURLConnection.HTTP_FORBIDDEN
    }
}