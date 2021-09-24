package com.demo.doccloud

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher

// delay -- time unit: ms
fun viewWait(delay: Long) {
    Espresso.onView(ViewMatchers.isRoot()).perform(waitFor(delay))
}

private fun waitFor(delay: Long):
        ViewAction = object : ViewAction {
    override fun perform(
        uiController: UiController?, view: View?
    ) { uiController?.loopMainThreadForAtLeast(delay) }

    override fun getConstraints(): Matcher<View> = ViewMatchers.isRoot()

    override fun getDescription():
            String = "wait for " + delay + "milliseconds"
}