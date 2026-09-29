package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.PersianUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("کنکور یار", appName)
    }

    @Test
    fun `test konkur percentage formula`() {
        // 20 questions, 15 correct, 3 wrong, 2 blank:
        // Percentage = ((15 * 3) - 3) / (20 * 3) * 100 = (45 - 3) / 60 * 100 = 42/60 * 100 = 70.0%
        val percentage = PersianUtils.calculateKonkurPercentage(correct = 15, wrong = 3, total = 20)
        assertEquals(70.0, percentage, 0.1)
    }

    @Test
    fun `test persian digits conversion`() {
        val converted = PersianUtils.toPersianDigits("1405-07-04")
        assertEquals("۱۴۰۵-۰۷-۰۴", converted)
    }
}
