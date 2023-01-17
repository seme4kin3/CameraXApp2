package com.example.cameraxapp2

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.palfs.cameraxinjava.GalleryAdapter
import android.widget.TextView
import android.os.Bundle

import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

import androidx.recyclerview.widget.GridLayoutManager
import com.palfs.cameraxinjava.ImagesGallery
import com.palfs.cameraxinjava.GalleryAdapter.PhotoListener
import android.widget.Toast
import android.content.Intent
import android.view.View
import android.widget.Button


class GalleryActivity : AppCompatActivity(), View.OnClickListener {
    var recyclerView: RecyclerView? = null
    var galleryAdapter: GalleryAdapter? = null
    var images: List<String>? = null
    var gallery_number: TextView? = null
    private lateinit var bToVideo: Button
    private lateinit var bToPhoto: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        gallery_number = findViewById(R.id.gallery_number)
        recyclerView = findViewById(R.id.recyclerview_gallery_images)
        bToVideo = findViewById(R.id.toVideo)
        bToPhoto = findViewById(R.id.toPhoto)
        bToPhoto.setOnClickListener(this)
        bToVideo.setOnClickListener(this)


        //check from permission
        if (ContextCompat.checkSelfPermission(
                this@GalleryActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@GalleryActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_READ_PERMISSION_CODE
            )
        } else {
            loadImages()
        }
    }

    private fun loadImages() {
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = GridLayoutManager(this, 4)
        images = ImagesGallery.listOfImages(this)

        recyclerView!!.adapter = galleryAdapter


        //
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_READ_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read external storage permission granted", Toast.LENGTH_SHORT)
                    .show()
                loadImages()
            } else {
                Toast.makeText(this, "Read external storage permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.toPhoto -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.toVideo -> {
                val intent2 = Intent(this, VideoActivity::class.java)
                startActivity(intent2)
            }
        }
    }

    companion object {
        private const val MY_READ_PERMISSION_CODE = 101
    }
}