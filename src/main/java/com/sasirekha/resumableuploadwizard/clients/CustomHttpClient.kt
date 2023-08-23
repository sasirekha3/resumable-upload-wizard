package com.sasirekha.resumableuploadwizard.clients

import android.util.Log
import com.sasirekha.resumableuploadwizard.builders.UploadWorkerHttpResponseBuilder
import com.sasirekha.resumableuploadwizard.exceptions.HttpRequestException
import com.sasirekha.resumableuploadwizard.models.UploadWorkerHttpRequest
import com.sasirekha.resumableuploadwizard.models.UploadWorkerHttpResponse
import okhttp3.*
import okhttp3.internal.Internal.logger
import java.io.IOException

class CustomHttpClient private constructor() {
    companion object {
        private val client = OkHttpClient.Builder()
            .addNetworkInterceptor(LoggingInterceptor())
            .build();
        private const val TAG = "CustomHttpClient"
        private var instance: CustomHttpClient? = null;
        fun getClient(): CustomHttpClient {
            if(instance == null) {
                instance = CustomHttpClient()
            }
            return instance as CustomHttpClient
        }
    }

    fun <T: UploadWorkerHttpResponseBuilder> performPut(putRequest: UploadWorkerHttpRequest, clazz: Class<T>): UploadWorkerHttpResponse {

        Log.d(TAG, "Uploading file chunk" );

        try{
            // Create request body
            val requestBody = RequestBody.create(
                MediaType.parse(putRequest.getContentType()),
                putRequest.getRequestBody()
            )

            // Add request body and url
            val requestBuilder = Request.Builder().put(requestBody).url(putRequest.getUrl())

            // Add headers
            for(header in putRequest.getCustomHeaders()){
                requestBuilder.addHeader(header.key, header.value)
            }

            val response: Response = client.newCall(requestBuilder.build()).execute();

            // Parse response
            val uploadWorkerHttpResponseBuilder = clazz.getConstructor().newInstance()
            uploadWorkerHttpResponseBuilder.setCorrespondingRequest(putRequest)
            uploadWorkerHttpResponseBuilder.setHeadersAndCookies(response.headers())
            uploadWorkerHttpResponseBuilder.setResponseCode(response.code())
            uploadWorkerHttpResponseBuilder.setBodyString(response.body().string())

            return uploadWorkerHttpResponseBuilder.build()
        } catch(exception: Exception) {
            Log.e(TAG, exception.message as String);
        }
        throw Exception("Resumable Upload Failed");
    }
    fun <T: UploadWorkerHttpResponseBuilder> performPost(postRequest: UploadWorkerHttpRequest, clazz: Class<T>): UploadWorkerHttpResponse {

        Log.d(TAG, "Fetching session url" );

        try {

            // Create request body
            val requestBody = RequestBody.create(
                MediaType.parse(postRequest.getContentType()),
                postRequest.getRequestBody()
            )

            // Add request body and url
            val requestBuilder = Request.Builder().post(requestBody).url(postRequest.getUrl())

            // Add headers
            for(header in postRequest.getCustomHeaders()){
                requestBuilder.addHeader(header.key, header.value)
            }

            // Add auth header
            requestBuilder.addHeader("x-api-key", BuildConfig.gcpapixapikey)

            val response: Response = client.newCall(requestBuilder.build()).execute();

            // Parse response
            val bodyString = response.body().string()
            val uploadWorkerHttpResponseBuilder = clazz.getConstructor().newInstance()
            uploadWorkerHttpResponseBuilder.setCorrespondingRequest(postRequest)
            uploadWorkerHttpResponseBuilder.setHeadersAndCookies(response.headers())
            uploadWorkerHttpResponseBuilder.setResponseCode(response.code())
            uploadWorkerHttpResponseBuilder.setBodyString(bodyString)

            Log.d(TAG, "Body String: ${bodyString}")

            return uploadWorkerHttpResponseBuilder.build()

        } catch(exception: Exception) {
            Log.e(TAG, exception.message as String);
        }

        throw HttpRequestException("Session URL Fetch Failed");
    }
}

internal class LoggingInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val t1 = System.nanoTime()
        logger.info(
            java.lang.String.format(
                "Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()
            )
        )
        val response = chain.proceed(request)
        val t2 = System.nanoTime()
        logger.info(
            String.format(
                "Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6, response.headers()
            )
        )
        return response
    }
}