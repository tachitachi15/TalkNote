package com.yuki.talknote

import android.util.Base64
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.lang.StringBuilder
import java.util.*


class SpeechToTextClient(val filename: String) {
    private val url = "https://speech.googleapis.com/v1/speech:recognize"
    private val apiKey = ""

    fun recognitionStart():String{
        val recognized = StringBuilder()
        runBlocking {
            async(Dispatchers.IO){
                OkHttpClient().newCall(createRequest(filename)).execute()
            }.await().let { response ->
                if (response.isSuccessful){
                    response.body()?.string().let { body->
                        val recognitionResponse = Gson().fromJson(body,RecognitionResponse::class.java)

                        recognitionResponse.results.forEach { result->
                            recognized.append(result.alternatives[0].transcript)
                        }
                    }
                }
            }
        }
        return recognized.toString()
    }


    private fun createRequest(filename: String) = Request.Builder()
        .url(url)
        .addHeader("X-Goog-Api-Key", apiKey)
        .addHeader("Content-Type", "application/json; charset=utf-8")
        .post(createRequestBody(encodeToBase64(filename)))
        .build()

    private fun createRequestBody(content: String): RequestBody {
        val requestParams = RequestParams(
            config = RecognitionConfig(
                encoding = "AMR",
                sampleRateHertz = 8000,
                languageCode = "ja-JP"
            ),
            audio = AudioData(
                content = content
            )
        )
        val json = Gson().toJson(requestParams)
        return RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            json
        )
    }

    private fun encodeToBase64(filename:String) = Base64.encodeToString(File(filename).inputStream().readBytes(),Base64.NO_WRAP)

    data class RequestParams(
        val config: RecognitionConfig,
        val audio: AudioData
    )

    data class RecognitionConfig(
        val encoding: String,
        val sampleRateHertz: Int,
        val languageCode: String
    )

    data class AudioData(
        val content: String
    )

    data class RecognitionResponse(
        val results: List<SpeechRecognitionResult>
    )

    data class SpeechRecognitionResult(
        val alternatives: List<SpeechRecognitionAlternatives>
    )

    data class SpeechRecognitionAlternatives(
        val confidence: Float,
        val transcript: String
    )
}
