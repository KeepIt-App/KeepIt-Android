package com.haero_kim.pickmeup.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.haero_kim.pickmeup.R
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.ui.ItemDetailActivity.Companion.EXTRA_ITEM
import com.haero_kim.pickmeup.util.Util.Companion.setErrorOnEditText
import com.haero_kim.pickmeup.viewmodel.ItemViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.random.Random


class AddActivity : AppCompatActivity() {

    // Koin 모듈을 활용한 ViewModel 인스턴스 생성
    private val itemViewModel: ItemViewModel by viewModel()

    var itemId: Long? = null
    private var itemImage: Uri? = null
    private lateinit var imageViewItemImage: ImageView

    lateinit var titleText: TextView
    lateinit var editTextItemName: EditText
    lateinit var editTextItemLink: EditText
    lateinit var editTextItemPrice: EditText
    lateinit var ratingItemPriority: RatingBar
    lateinit var editTextItemMemo: EditText
    lateinit var completeButton: CardView
    lateinit var backButton: ImageButton

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

    /**
     * Bitmap 이미지를 Local 에 저장하고, URI 를 반환함
     **/
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

        titleText = findViewById(R.id.title)
        editTextItemName = findViewById<EditText>(R.id.itemName)
        imageViewItemImage = findViewById<ImageView>(R.id.itemImage)
        editTextItemLink = findViewById<EditText>(R.id.itemLink)
        editTextItemPrice = findViewById<EditText>(R.id.itemPrice)
        ratingItemPriority = findViewById<RatingBar>(R.id.itemRatingBar)
        editTextItemMemo = findViewById<EditText>(R.id.itemMemo)
        completeButton = findViewById<CardView>(R.id.completeButton)
        backButton = findViewById<ImageButton>(R.id.backButton)

        /**
         * 아이템 편집 기능을 통해 들어온 것이라면 기존 정보 적용
         */
        if (intent != null && intent.hasExtra(EDIT_ITEM)) {
            applyExistingInfo(intent.getSerializableExtra(EDIT_ITEM) as ItemEntity)
        }

        // ImageView 를 눌렀을 때 이미지 추가 액티비티로 이동
        imageViewItemImage.setOnClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle("이미지 추가")
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setCropMenuCropButtonTitle("완료")
                .setRequestedSize(1280, 900)
                .start(this)
        }

        backButton.setOnClickListener { finish() }

        // 작성 완료 버튼을 눌렀을 때
        completeButton.setOnClickListener {
            val itemName = editTextItemName.text.toString().trim()
            val itemImage = itemImage.toString()  // Uri 를 String 으로 변환한 형태
            val itemLink = editTextItemLink.text.toString().trim()
            val itemPrice = editTextItemPrice.text.toString().trim()
            val itemPriority = ratingItemPriority.rating.toInt()
            val itemMemo = editTextItemMemo.text.toString().trim()

            // Valid Check
            // TODO : 코드가 비효율적으로 보이지만, 이렇게 해야 두 EditText 가 모두 비었을 때 둘 다 에러가 적용된다. (더 나은 방법 탐색 필요)
            //  setErrorOnEditText() 는 해당 EditText 에 특정 Error 를 뿌려줌
            if (itemName.isEmpty() || itemPrice.isEmpty()) {
                if (itemName.isEmpty()) {
                    setErrorOnEditText(editTextItemName, resources.getText(R.string.itemNameError))
                }
                if (itemPrice.isEmpty()) {
                    setErrorOnEditText(editTextItemPrice, resources.getText(R.string.itemPriceError))
                }
            } else {
                val builder = AlertDialog.Builder(this)
                builder.apply {
                    this.setMessage(resources.getText(R.string.completeDialog))
                    this.setNegativeButton("NO") { _, _ -> }
                    this.setPositiveButton("YES") { _, _ ->
                        val newItem = ItemEntity(
                            id = itemId,  // 새로운 Item 이면 Null 들어감 (자동 값 적용)
                            name = itemName,
                            image = itemImage,
                            price = itemPrice.toLong(),
                            link = itemLink,
                            priority = itemPriority,
                            note = itemMemo
                        )
                        itemViewModel.insert(newItem)

                        // 수정된 내용을 사용자게에 보여줌
                        val intent = Intent(context, ItemDetailActivity::class.java)
                        intent.putExtra(EXTRA_ITEM, newItem)
                        startActivity(intent)
                        finish()
                    }
                }
                builder.show()
            }
        }
    }

    /**
     * EditText 가 아닌 곳을 터치하면 키보드 내림
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val focusView: View? = currentFocus
        if (focusView != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev.x.toInt()
            val y = ev.y.toInt()
            if (!rect.contains(x, y)) {
                val imm: InputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                focusView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 아이템 신규 생성이 아닌 편집 기능인 경우 기존 정보 채워줌
     */
    private fun applyExistingInfo(item: ItemEntity) {
        titleText.text = "수정하기"
        itemId = item.id
        editTextItemName.setText(item.name)
        editTextItemLink.setText(item.link)
        editTextItemPrice.setText(item.price.toString())
        ratingItemPriority.rating = item.priority.toFloat()
        editTextItemMemo.setText(item.note)
        itemImage = Uri.parse(item.image)
        imageViewItemImage.setImageURI(itemImage)
    }

    companion object {
        const val EDIT_ITEM: String = "EDIT_ITEM"
    }
}