package com.vuongvanduy.music_app.ui.settings.appearance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.vuongvanduy.music_app.R
import com.vuongvanduy.music_app.base.fragment.BaseFragment
import com.vuongvanduy.music_app.common.DARK_MODE
import com.vuongvanduy.music_app.common.LIGHT_MODE
import com.vuongvanduy.music_app.common.SYSTEM_MODE
import com.vuongvanduy.music_app.common.TITLE_APPEARANCE
import com.vuongvanduy.music_app.databinding.FragmentAppearanceBinding

class AppearanceFragment : BaseFragment() {

    private lateinit var binding: FragmentAppearanceBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppearanceBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLayout()

        initListener()
    }

    private fun setLayout() {
        when (mainViewModel.themeMode.value) {
            SYSTEM_MODE -> binding.rdBtGroup.check(R.id.rd_bt_follow_system)

            LIGHT_MODE -> binding.rdBtGroup.check(R.id.rd_bt_light)

            DARK_MODE -> binding.rdBtGroup.check(R.id.rd_bt_dark)
        }
    }

    private fun initListener() {
        binding.btApply.setOnClickListener {
            changeThemeMode(binding.rdBtGroup.checkedRadioButtonId)
        }
    }

    private fun changeThemeMode(position: Int) {
        when (position) {
            R.id.rd_bt_follow_system ->
                setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, SYSTEM_MODE)

            R.id.rd_bt_light ->
                setThemeMode(AppCompatDelegate.MODE_NIGHT_NO, LIGHT_MODE)

            R.id.rd_bt_dark ->
                setThemeMode(AppCompatDelegate.MODE_NIGHT_YES, DARK_MODE)
        }
    }

    private fun setThemeMode(mode: Int, value: String) {
        AppCompatDelegate.setDefaultNightMode(mode)
        mainViewModel.themeMode.value = value
    }

    override fun onResume() {
        super.onResume()
        mainActivity.binding.toolBarTitle.text = TITLE_APPEARANCE
    }
}