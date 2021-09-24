package com.demo.doccloud

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.demo.doccloud.ui.home.adapters.DocAdapter
import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not

/**
 *
 * Perform Methods:
 *
 * 1 - performLongClickOnRecyclerViewItem()
 * 2 - performClickOnRecyclerViewItem()
 * 3 - performClickOnText()
 * 4 - performClickOnView
 * 5 - performMenuItemClick()
 *
 * Check Methods
 *
 * 1 - checkTextOnAlertDialog()
 * 2 - checkTextOnScreen()
 * 3 - checkSizeOnRecyclerView()
 * 5 - checkTextOnTextView()
 * 6 - checkTextOnButton()
 * 7 - checkIsVisible()
 * 8 - checkIsNotVisible()
 *
 * Others Methods
 *
 * 1 - typeTextOnEditText()
 */

class EspressoActions {
    companion object{
        fun performLongClickOnRecyclerViewItem(rvId: Int, position: Int){
            onView(withId(rvId))
                .perform(
                    RecyclerViewActions
                        .actionOnItemAtPosition<DocAdapter.DocAdapterViewHolder>(
                            position,
                            ViewActions.longClick()
                        )
                )
        }
        fun performClickOnRecyclerViewItem(rvId: Int, position: Int){
            onView(withId(rvId))
                .perform(
                    RecyclerViewActions
                        .actionOnItemAtPosition<DocAdapter.DocAdapterViewHolder>(
                            position,
                            ViewActions.click()
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
        fun checkSizeOnRecyclerView(rv: Int, size: Int){
            onView(withId(rv)).check(matches(hasItemCountOnRecyclerView(size)))
        }
        fun checkTextOnTextView(viewId: Int, text: String){
            onView(withId(viewId)).check(matches(withText(text)))
        }
        fun checkTextOnButton(buttonId: Int, text: String){
            onView(withId(buttonId)).check(matches(withText(text)))
        }
        fun checkIsVisible(viewId: Int){
            onView(withId(viewId)).check(matches(isDisplayed()))
        }

        fun checkIsNotVisible(viewId: Int){
            onView(withId(viewId)).check(matches(not(isDisplayed())))
        }

        fun typeTextOnEditText(editTextId: Int, text: String){
            onView(withId(editTextId)).perform(ViewActions.typeText(text))
        }

        //https://stackoverflow.com/questions/36399787/how-to-count-recyclerview-items-with-espresso
        @JvmStatic
        private fun hasItemCountOnRecyclerView(itemCount: Int): Matcher<View> {
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