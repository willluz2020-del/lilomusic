package com.vmb.music

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vmb.music.databinding.ItemTrackBinding

class TrackAdapter(
    private val tracks: MutableList<Track>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    inner class TrackViewHolder(val binding: ItemTrackBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.binding.trackTitle.text = track.title
        holder.binding.trackSubtitle.text = "MP3 local"
        holder.binding.root.setOnClickListener { onClick(position) }
        holder.binding.playTrack.setOnClickListener { onClick(position) }
    }

    override fun getItemCount(): Int = tracks.size

    fun refresh() = notifyDataSetChanged()
}
