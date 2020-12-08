package xandeer.android.synclip

import android.content.Context
import android.view.LayoutInflater
import android.view.View

inline val Context.layoutInflater: LayoutInflater
  get() = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

inline val View.layoutInflater get() = context.layoutInflater
