package com.vuongvanduy.music_app.ui.settings

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vuongvanduy.music_app.R
import com.vuongvanduy.music_app.base.fragment.BaseFragment
import com.vuongvanduy.music_app.common.SETTINGS_FRAGMENT_TAG
import com.vuongvanduy.music_app.databinding.FragmentSettingsBinding
import com.vuongvanduy.music_app.ui.settings.account.AccountFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        init()

        return binding.root
    }
}