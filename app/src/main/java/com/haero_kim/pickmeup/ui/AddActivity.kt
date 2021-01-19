package com.haero_kim.pickmeup.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.haero_kim.pickmeup.R
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.viewmodel.ItemViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Math.abs
import kotlin.random.Random
import kotlin.random.nextUInt

class AddActivity : AppCompatActivity() {

    private lateinit var itemViewModel: ItemViewModel
    private var viewModelFactory: ViewModelProvider.AndroidViewModelFactory? = null
    private var itemImage: Uri? = null
    private lateinit var imageViewItemImage: ImageView

    /**
     * 사용자가 이미지 선택을 완료하면 실행됨
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 업로드를 위한 사진이 선택 및 편집되면 Uri 형태로 결과가 반환됨
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                val bitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
                itemImage = bitmapToFile(bitmap!!) // Uri
                imageViewItemImage.setImageURI(itemImage)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.e("Error Image Selecting", "이미지 선택 및 편집 오류")
            }
        }
    }

    /**  Bitmap 이미지를 Local에 저장하고, URI를 반환함  **/
    private fun bitmapToFile(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(this)
        val randomNumber = Random.nextInt(0, 1000000000).toString()
        // Bitmap 파일 저장을 위한 File 객체
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "item_${randomNumber}.jpg")
        try {
            // Bitmap 파일을 JPEG 형태로 압축해서 출력
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Error Saving Image", e.message!!)
        }
        return Uri.parse(file.absolutePath)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        val editTextItemName = findViewById<EditText>(R.id.itemName)
        imageViewItemImage = findViewById<ImageView>(R.id.itemImage)
        val editTextItemLink = findViewById<EditText>(R.id.itemLink)
        val editTextItemPrice = findViewById<EditText>(R.id.itemPrice)
        val ratingItemPriority = findViewById<RatingBar>(R.id.itemRatingBar)
        val editTextItemMemo = findViewById<EditText>(R.id.itemMemo)
        val completeButton = findViewById<CardView>(R.id.completeButton)

        if (viewModelFactory == null) {
            viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        }

        itemViewModel = ViewModelProvider(this, viewModelFactory!!).get(ItemViewModel::class.java)

        imageViewItemImage.setOnClickListener {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setActivityTitle("이미지 추가")
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setCropMenuCropButtonTitle("완료")
                    .setRequestedSize(1280, 900)
                    .start(this)
        }

        // 작성 완료 버튼을 눌렀을 때
        completeButton.setOnClickListener {
            val itemName = editTextItemName.text.toString().trim()
            val itemImage = itemImage.toString()  // Uri 를 String 으로 변환한 형태
            val itemLink = editTextItemLink.text.toString().trim()
            val itemPrice = editTextItemPrice.text.toString().trim()
            val itemPriority = ratingItemPriority.rating.toInt()
            val itemMemo = editTextItemMemo.text.toString().trim()

            // Valid Check
            if (itemName.isEmpty()) {
                editTextItemName.error = "이름은 필수 입력 항목입니다"
            } else if (itemPrice.isEmpty()) {
                editTextItemPrice.error = "가격은 필수 입력 항목입니다"
            } else {
                val builder = AlertDialog.Builder(this)
                builder.apply {
                    this.setMessage("작성을 완료하시겠습니까?")
                    this.setNegativeButton("NO") { _, _ -> }
                    this.setPositiveButton("YES") { _, _ ->
                        val newItem = ItemEntity(
                                id = null,
                                name = itemName,
                                image = itemImage,
                                price = itemPrice.toLong(),
                                link = itemLink,
                                priority = itemPriority,
                                note = itemMemo
                        )
                        itemViewModel.insert(newItem)
                        finish()
                    }
                }
                builder.show()

            }
        }

    }

}