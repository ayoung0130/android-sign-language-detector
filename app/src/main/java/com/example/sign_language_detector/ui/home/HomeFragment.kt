package com.example.sign_language_detector.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sign_language_detector.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _fragmentHomeBinding: FragmentHomeBinding? = null
    private val fragmentHomeBinding
        get() = _fragmentHomeBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentHomeBinding =
            FragmentHomeBinding.inflate(inflater, container, false)

        return fragmentHomeBinding.root
    }
}