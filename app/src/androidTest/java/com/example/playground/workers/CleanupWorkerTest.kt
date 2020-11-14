package com.example.playground.workers

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import com.example.playground.workmanager.workers.CleanUpWorker
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class CleanupWorkerTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var workManagerRule = WorkManagerTestRule()

    @Test
    fun testCleanupWork() {
        val testUri = copyFileFromTestToTargetCtx(
            workManagerRule.testContext, workManagerRule.targetContext, "test_image.png"
        )
        assertThat(
            uriFileExists(
                workManagerRule.targetContext,
                "$testUri"
            ), `is`(true)
        )

        // Create request
        val request = OneTimeWorkRequestBuilder<CleanUpWorker>().build()

        // Enqueue and wait for result. This also runs the Worker synchronously
        // because we are using a SynchronousExecutor
        workManagerRule.workManager.enqueue(request).result.get()
        // Get WorkInfo
        val workInfo = workManagerRule.workManager.getWorkInfoById(request.id).get()
        // Assert
        assertThat(
            uriFileExists(
                workManagerRule.targetContext,
                "$testUri"
            ), `is`(true)
        )
        assertThat(
            workInfo.state,
            `is`(WorkInfo.State.SUCCEEDED)
        )
    }
}