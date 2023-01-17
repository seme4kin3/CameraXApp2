package com.palfs.cameraxinjava

import android.content.Context
import com.palfs.cameraxinjava.GalleryAdapter.PhotoListener
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.example.cameraxapp2.R

class GalleryAdapter(
    private val context: Context,
    private val images: List<String>,
    protected var photoListener: PhotoListener
) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.gallery_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = images[position]
        Glide.with(context).load(image).into(holder.image)
        holder.itemView.setOnClickListener { photoListener.onPhotoClick(image) }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView

        init {
            image = itemView.findViewById(R.id.image)
        }
    }

    interface PhotoListener {
        fun onPhotoClick(path: String?)
    }
}