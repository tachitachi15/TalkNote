package com.yuki.talknote

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_second.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    private val args: SecondFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val talk = args.talkItem
        val dateTextView = view.findViewById<TextView>(R.id.date_text)
        val keywordsTextView = view.findViewById<TextView>(R.id.keywords_text)
        val imageView = view.findViewById<ImageView>(R.id.image)
        val reloadButton = view.findViewById<ImageView>(R.id.reload_image)

        imageView.scaleType = ImageView.ScaleType.FIT_XY

        val imageUris:List<Uri> = runBlocking {
            queryImages(talk.date)
        }

        var bitmap = getBitmapByUris(imageUris)

        Log.i("coroutine",bitmap.toString())
        bitmap?.let { imageView.setImageBitmap(it) }


        dateTextView.text = talk.date
        keywordsTextView.text = talk.keywords

        //更新かかったら再びランダムに画像取得
        reloadButton.setOnClickListener{
            bitmap = getBitmapByUris(imageUris)
            Log.i("coroutine",bitmap.toString())
            bitmap?.let { imageView.setImageBitmap(it) }
        }

    }

    private suspend fun queryImages(dateForSearch: String): List<Uri> {
        val imageUris = mutableListOf<Uri>()

        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_TAKEN
            )
            val selection = "${MediaStore.Images.Media.DATE_TAKEN} >= ? and ${MediaStore.Images.Media.DATE_TAKEN} <= ?"
            val nowToLong = stringToLongDatetime(dateForSearch)
            val selectionArgs = arrayOf(
                //記録時間から前後２時間が検索範囲
                (nowToLong-hoursToMilliSeconds(2,0,0)).toString(),(nowToLong+hoursToMilliSeconds(2,0,0)).toString()
            )
            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

            val cursor = context?.contentResolver?.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                val idColumn = it.getColumnIndex(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    imageUris += contentUri
                }
            }
        }
        return imageUris
    }

    private fun getBitmapByUris(imageUris:List<Uri>):Bitmap? {

        val resolver = context?.contentResolver
        var image: Bitmap? = null

        if (imageUris.isNotEmpty()) {
            resolver?.openInputStream(imageUris.random()).use { stream ->
                //Log.i("storage image stream",stream.toString())
                image = BitmapFactory.decodeStream(stream)
            }
        }
        return image
    }

    private fun stringToLongDatetime(dateStr:String):Long = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dateStr)?.time ?:0
    //getTime() January 1, 1970, 00:00:00 GMTからの経過時間をミリseconds単位で取得

    private fun hoursToMilliSeconds(hour:Int,minutes:Int,seconds:Int):Long = (hour*3600 + minutes*60 + seconds)*1000.toLong()
}
