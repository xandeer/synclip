package xandeer.android.synclip

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager

inline val Context.layoutInflater: LayoutInflater
  get() = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

inline val View.layoutInflater get() = context.layoutInflater

fun Activity.hideSoftInput() {
  currentFocus?.let {
    it.clearFocus()
    (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?)
      ?.hideSoftInputFromWindow(it.applicationWindowToken, 0)
  }
}

fun View.hideSoftInput() {
  (context as Activity).hideSoftInput()
}