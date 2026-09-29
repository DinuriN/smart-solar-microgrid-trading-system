package com.example.solaragrid.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.solaragrid.R

class ComingSoonFragment : Fragment() {

    companion object {
        fun newInstance(title: String): ComingSoonFragment {
            val fragment = ComingSoonFragment()
            val args = Bundle()
            args.putString("title", title)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_coming_soon, container, false)
        val title = arguments?.getString("title") ?: "Tab"
        view.findViewById<TextView>(R.id.tvTitle).text = "$title\nComing Soon"
        return view
    }
}
