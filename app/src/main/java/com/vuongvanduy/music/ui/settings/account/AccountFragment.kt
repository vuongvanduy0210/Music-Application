package com.vuongvanduy.music.ui.settings.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vuongvanduy.music.R
import com.vuongvanduy.music.base.fragment.BaseMainFragment
import com.vuongvanduy.music.common.GUEST
import com.vuongvanduy.music.common.GUEST_EMAIL
import com.vuongvanduy.music.common.TITLE_ACCOUNT
import com.vuongvanduy.music.common.TITLE_FAVOURITE_SONGS
import com.vuongvanduy.music.common.sendListSongToService
import com.vuongvanduy.music.data.models.Song
import com.vuongvanduy.music.databinding.FragmentAccountBinding

class AccountFragment : BaseMainFragment() {

    override val TAG = AccountFragment::class.java.simpleName.toString()

    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    override fun init() {
        super.init()
        binding.viewModel = accountViewModel
        binding.lifecycleOwner = viewLifecycleOwner
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListener()

        registerObserver()
    }

    private fun initListener() {
        binding.apply {
            btSignIn.setOnClickListener {
                mainActivity.goToLoginActivity()
            }

            btChangeProfile.setOnClickListener {
                val action = AccountFragmentDirections.actionAccountFragmentToProfileFragment()
                findNavController().navigate(action)
            }

            btChangePassword.setOnClickListener {
                val action =
                    AccountFragmentDirections.actionAccountFragmentToChangePasswordFragment()
                findNavController().navigate(action)
            }

            btSignOut.setOnClickListener {
                onClickSignOut()
            }
        }
    }

    private fun onClickSignOut() {
        AlertDialog.Builder(mainActivity)
            .setTitle("Sign Out")
            .setMessage("Are you sure want to sign out?")
            .setPositiveButton("Yes") { _, _ ->
                Firebase.auth.signOut()
                accountViewModel.user.value = null
                songViewModel.favouriteSongs.value = null
                songViewModel.deleteAllFavourites()
                if (mainViewModel.currentListName == TITLE_FAVOURITE_SONGS) {
                    val list = mutableListOf<Song>()
                    sendListSongToService(mainActivity, list)
                }
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
    }

    private fun registerObserver() {
        accountViewModel.user.observe(viewLifecycleOwner) { user ->
            accountViewModel.isShowSignOut.postValue(user != null)
            if (user != null) {
                binding.apply {
                    Glide.with(mainActivity).load(user.photoUrl).error(R.drawable.img_avatar_error)
                        .into(imgUser)
                    tvName.text = user.displayName
                    tvEmail.text = user.email

                    songViewModel.getFavouriteSongsFromRemote()
                }
            } else {
                binding.apply {
                    Glide.with(mainActivity).load(R.drawable.img_avatar_error)
                        .into(imgUser)
                    tvName.text = GUEST
                    tvEmail.text = GUEST_EMAIL
                }

                songViewModel.favouriteSongs.value = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainActivity.binding.toolBarTitle.text = TITLE_ACCOUNT

        val user = FirebaseAuth.getInstance().currentUser
        accountViewModel.user.postValue(user)
    }
}