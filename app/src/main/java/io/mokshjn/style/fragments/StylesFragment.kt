package io.mokshjn.style.fragments

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.mokshjn.style.R

/**
 * Created by moksh on 28/6/17.
 */

class StylesFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater?.inflate(R.layout.fragment_styles, container, false)

        return root
    }

}