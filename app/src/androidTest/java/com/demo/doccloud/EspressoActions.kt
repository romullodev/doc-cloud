package com.demo.doccloud

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import com.demo.doccloud.ui.home.adapters.DocAdapter
import org.hamcrest.CoreMatchers

import androidx.test.espresso.Espresso.onView

import androidx.test.espresso.matcher.RootMatchers.withDecorView

import android.R
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher


class EspressoActions {
    companion object{
        fun performLongClickRecyclerViewItem(rvId: Int, position: Int){
            onView(withId(rvId))
                .perform(
                    RecyclerViewActions
                        .actionOnItemAtPosition<DocAdapter.DocAdapterViewHolder>(
                            position,
                            ViewActions.longClick()
                        )
                )
        }
        fun performClickOnText(stringId: Int){
            onView(withText(stringId)).perform(
                ViewActions.click()
            )
        }
        fun performClickOnView(viewId: Int){
            onView(withId(viewId)).perform(
                ViewActions.click()
            )
        }
        fun performMenuItemClick(resourceName: Int, menuId: Int){
            onView(
                CoreMatchers.anyOf(
                    withText(resourceName),
                    withId(menuId)
                )
            ).perform(ViewActions.click())
        }

        fun checkTextOnAlertDialog(resourceText: Int){
            onView(withText(resourceText))
                .inRoot(RootMatchers.isDialog())
                .check(
                    matches(
                        isDisplayed()
                    )
                )
        }

        fun checkTextOnScreen(resourceText: Int){
            onView(withText(resourceText)).check(
                matches(isDisplayed())
            )
        }

        //https://stackoverflow.com/questions/36399787/how-to-count-recyclerview-items-with-espresso
        @JvmStatic
        fun hasItemCountOnRecyclerView(itemCount: Int): Matcher<View> {
            return object : BoundedMatcher<View, RecyclerView>(
                RecyclerView::class.java) {

                override fun describeTo(description: Description) {
                    description.appendText("has $itemCount items")
                }

                override fun matchesSafely(view: RecyclerView): Boolean {
                    return view.adapter?.itemCount == itemCount
                }
            }
        }
    }
}