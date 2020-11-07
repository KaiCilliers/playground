package com.example.playground.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.playground.room.AppDatabase
import com.example.playground.room.Plant
import com.example.playground.util.constants.PLANT_DATA_FILENAME
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

/**
 * Responsible for populating the database
 * from a json file
 */
class DatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            // Access the json file in the asset directory
            applicationContext.assets.open("${PLANT_DATA_FILENAME()}").use { inputStream ->
                JsonReader(inputStream.reader()).use { jsonReader ->
                    /**
                     * TypeToken is used to tell Gson what exactly
                     * you want your string to get converted to
                     *
                     * Ref: https://stackoverflow.com/questions/43117731/what-is-type-typetoken
                     * Ref: https://helw.net/2017/11/09/runtime-generics-in-an-erasure-world/
                     *
                     * Jist is that you tell Gson you want your JSON
                     * to be converted to the type List<Plant>
                     *
                     * Generics are a compile time concept to help
                     * enforce type safety. During compilation, type
                     * erasure kicks in which results in hte underlying
                     * bytecode being free of any generics information
                     *
                     * Here you require the generics information at runtime
                     * and TypeToken helps solve this use case
                     */
                    val plantType = object : TypeToken<List<Plant>>() {}.type
                    val plantList: List<Plant> = Gson().fromJson(jsonReader, plantType)

                    val database = AppDatabase.getInstance(applicationContext)
                    database.dao().insertAll(plantList)

                    Result.success()
                }
            }
        } catch (e: Exception) {
            Timber.e("Error seeding database: $e")
            Result.failure()
        }
    }
}
// // Simply an example of waht the calss would look like
// // if you did not use an anonymous class
//class MyTypeToken : TypeToken<List<Plant>>() {
//
//}