package com.palfs.cameraxinjava

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import java.util.ArrayList

object ImagesGallery {
    fun listOfImages(context: Context): ArrayList<String> {
        val cursor: Cursor?
        val cursor2: Cursor?
        val column1: Int
        val column2: Int
        val listOfAllImages = ArrayList<String>()
        var ablosutePathOfImage: String
        var ablosutePathOfImage2: String
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val uri2 = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )
        val orderBy = MediaStore.Images.Media.DATE_TAKEN
        val orderBy2 = MediaStore.Video.Media.DATE_TAKEN
        cursor = context.contentResolver.query(
            uri2, projection, null,
            null, "$orderBy DESC"
        )
        cursor2 = context.contentResolver.query(
            uri, projection, null,
            null, "$orderBy2 DESC"
        )
        column1 = cursor!!.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)
        column2 = cursor2!!.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA)


        // Cursor cursorall = new MergeCursor(new Cursor[]{cursor, cursor2});
        while (cursor.moveToNext()) {
            ablosutePathOfImage = cursor.getString(column1)
            listOfAllImages.add(ablosutePathOfImage)
        }
        while (cursor2.moveToNext()) {
            ablosutePathOfImage2 = cursor2.getString(column2)
            listOfAllImages.add(ablosutePathOfImage2)
        }
        cursor.close()
        return listOfAllImages
    }
}