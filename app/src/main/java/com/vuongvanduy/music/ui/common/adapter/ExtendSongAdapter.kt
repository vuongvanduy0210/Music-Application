package com.vuongvanduy.music.ui.common.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vuongvanduy.music.common.*
import com.vuongvanduy.music.data.common.containsIgnoreCaseWithDiacritics
import com.vuongvanduy.music.data.models.Song
import com.vuongvanduy.music.databinding.ItemExtendSongBinding
import com.vuongvanduy.music.ui.common.myinterface.IClickSongListener

class ExtendSongAdapter constructor(
    private val iClickSongListener: IClickSongListener,
    private val name: String
) : RecyclerView.Adapter<ExtendSongAdapter.SongViewHolder>(), Filterable {

    private var songs: List<Song>? = null
    private var listSongsOld: List<Song>? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<Song>) {
        this.songs = list
        this.listSongsOld = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemExtendSongBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SongViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (!songs.isNullOrEmpty()) {
            songs!!.size
        } else 0
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        if (songs?.isEmpty() == true) {
            return
        }
        holder.binding.layoutItemOnlineSong.close(false)
        val song = songs?.get(position)
        if (song != null) {

            holder.bind(song)

            holder.binding.apply {
                Glide.with(this.root)
                    .load(Uri.parse(song.imageUri))
                    .into(imgMusicInList)

                holder.binding.tvAction.text = if (name == TITLE_FAVOURITE_SONGS) {
                    TEXT_REMOVE_FAVOURITES
                } else {
                    TEXT_ADD_FAVOURITES
                }

                layoutAddFavourites.setOnClickListener {
                    iClickSongListener.onClickExtendFavourites(song)
                    holder.binding.layoutItemOnlineSong.close(true)
                }
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val strSearch = constraint.toString()
                songs = if (strSearch.isEmpty()) {
                    listSongsOld
                } else {
                    val list = ArrayList<Song>()
                    listSongsOld?.forEach {
                        if (it.name?.let { it1 ->
                                containsIgnoreCaseWithDiacritics(it1, strSearch)
                            } == true && !isSongExists(list, it)) {
                            list.add(it)
                        }

                        if (it.singer?.let { it1 ->
                                containsIgnoreCaseWithDiacritics(it1, strSearch)
                            } == true && !isSongExists(list, it)) {
                            list.add(it)
                        }
                    }
                    list
                }

                val filterResult = FilterResults()
                filterResult.values = songs
                return filterResult
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results?.values != null) {
                    songs = results.values as List<Song>
                }
                notifyDataSetChanged()
            }
        }
    }

    inner class SongViewHolder constructor(val binding: ItemExtendSongBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {
            binding.song = song
            binding.listener = iClickSongListener
            binding.executePendingBindings()
        }
    }

}