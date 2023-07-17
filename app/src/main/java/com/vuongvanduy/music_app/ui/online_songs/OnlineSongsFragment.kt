package com.vuongvanduy.music_app.ui.online_songs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vuongvanduy.music_app.activites.MainActivity
import com.vuongvanduy.music_app.activites.MainViewModel
import com.vuongvanduy.music_app.base.fragment.BaseFragment
import com.vuongvanduy.music_app.common.*
import com.vuongvanduy.music_app.data.models.Song
import com.vuongvanduy.music_app.databinding.FragmentOnlineSongsBinding
import com.vuongvanduy.music_app.ui.common.adapter.ExtendSongAdapter
import com.vuongvanduy.music_app.ui.common.myinterface.IClickSongListener
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OnlineSongsFragment : BaseFragment() {

    private lateinit var binding: FragmentOnlineSongsBinding

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                mainViewModel.currentSong.value?.let { playMusic(it) }
            } else {
                Log.e("FRAGMENT_NAME", "Permission denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songViewModel.getListOnline()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnlineSongsBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun init() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = songViewModel
        mainActivity = requireActivity() as MainActivity
        mainViewModel = ViewModelProvider(mainActivity)[MainViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclerViewSongs()

        registerObserverFetchDataFinish()

        setOnClickBtSearchView()
    }

    private fun setRecyclerViewSongs() {
        extendSongAdapter = ExtendSongAdapter(object : IClickSongListener {
            override fun onClickSong(song: Song) {
                mainViewModel.currentSong.postValue(song)
                playSong(song)
            }

            override fun onClickAddFavourites(song: Song) {
//                addToFavourites(currentSong)
            }

            override fun onClickRemoveFavourites(song: Song) {}
        }, TITLE_ONLINE_SONGS)
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.rcvListSongs.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(decoration)
            adapter = extendSongAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        hideKeyboard(requireContext(), binding.root)
                    }
                }
            })
        }
    }

    private fun playSong(song: Song) {
        mainViewModel.isShowMusicPlayer.postValue(true)
        mainViewModel.isServiceRunning.postValue(true)
        hideKeyboard(requireContext(), binding.root)
        requestPermissionPostNotification(song)
    }

    private fun requestPermissionPostNotification(song: Song) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (mainActivity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                playMusic(song)
            } else {
                activityResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            playMusic(song)
        }
    }

    private fun playMusic(song: Song) {
        mainViewModel.currentListName = TITLE_ONLINE_SONGS
        songViewModel.onlineSongs.value?.let { sendListSongToService(mainActivity, it) }
        sendDataToService(mainActivity, song, ACTION_START)
    }

    private fun registerObserverFetchDataFinish() {
        songViewModel.onlineSongs.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                songViewModel.isLoadingOnline.postValue(false)
                extendSongAdapter.setData(it)
            }
        }
    }

    private fun setOnClickBtSearchView() {
        binding.imgClear.apply {
            setOnClickListener {
                binding.edtSearch.setText("")
            }
        }
        binding.edtSearch.apply {
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard(requireContext(), binding.root)
                    return@setOnEditorActionListener true
                }
                false
            }

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    extendSongAdapter.filter.filter(s)
                }
            })
        }
    }
}