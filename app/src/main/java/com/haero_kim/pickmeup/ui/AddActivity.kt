package com.haero_kim.pickmeup.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.anandwana001.ogtagparser.LinkSourceContent
import com.anandwana001.ogtagparser.LinkViewCallback
import com.anandwana001.ogtagparser.OgTagParser
import com.bumptech.glide.Glide
import com.haero_kim.pickmeup.R
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.databinding.ActivityAddBinding
import com.haero_kim.pickmeup.ui.ItemDetailActivity.Companion.EXTRA_ITEM
import com.haero_kim.pickmeup.util.Util.Companion.setErrorOnEditText
import com.haero_kim.pickmeup.viewmodel.ItemViewModel
import com.haero_kim.pickmeup.worker.NotificationWorker
import com.haero_kim.pickmeup.worker.NotificationWorker.Companion.ITEM_NAME
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.random.Random

private var itemName: String = ""
private var itemLink: String = ""
private var itemPrice: String = ""
private var itemPriority: Int = 0
private var itemMemo: String = ""

class AddActivity : AppCompatActivity() {
    // Koin 모듈을 활용한 ViewModel 인스턴스 생성
    private val itemViewModel: ItemViewModel by viewModel()
    lateinit var binding: ActivityAddBinding

    // 물건의 가격을 입력하는 EditText 에 화폐 단위 표시를 하기위한 DecimalFormat
    private val decimalFormat = DecimalFormat("#,###")
    private var formatPriceResult = ""

    var itemId: Long? = null
    private var itemImage: Uri? = null
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
                binding.imageViewItemImage.setImageURI(itemImage)

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

        binding = ActivityAddBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        /**
         * 아이템 편집 기능을 통해 인텐트 된 것이라면 기존 정보 삽입
         */
        if (intent != null && intent.hasExtra(EDIT_ITEM)) {
            applyExistingInfo(intent.getSerializableExtra(EDIT_ITEM) as ItemEntity)
        }

        /**
         * 링크 자동 인식 기능을 통해 들어온 것이라면 링크 정보 적용
         */
        if (intent != null && intent.hasExtra(AUTO_ITEM)) {
            var itemLink = intent.getStringExtra(AUTO_ITEM) ?: ""
            binding.editTextItemLink.setText(itemLink)
            when {
                Patterns.WEB_URL.matcher(itemLink).matches() -> {
                    // 만약 http 형식이 아니라면 앞에 'http://' 를 붙여줘야함 ==> ex) www.naver.com 과 같은 상황
                    if (!URLUtil.isHttpsUrl(itemLink) and !URLUtil.isHttpUrl(itemLink)){
                        itemLink = "https://" + itemLink
                    }
                    // Open Graph 태그를 불러오는 라이브러리 사용
                    OgTagParser().execute(itemLink, object : LinkViewCallback {
                        override fun onAfterLoading(linkSourceContent: LinkSourceContent) {
                            Log.d("TEST", linkSourceContent.ogTitle)
                            Log.d("TEST", linkSourceContent.ogDescription)
                            Log.d("TEST", linkSourceContent.images)

                            val siteTitle = linkSourceContent.ogTitle
                            val siteDescription = linkSourceContent.ogDescription
                            var siteThumbnail = linkSourceContent.images

                            if (siteTitle.isNotBlank() or siteDescription.isNotBlank() or siteThumbnail.isNotBlank()){
                                if (siteThumbnail.startsWith("//")) {
                                    siteThumbnail = "https:" + siteThumbnail
                                }

                                val builder = AlertDialog.Builder(this@AddActivity)
                                builder.setMessage("사이트에서 발견한 콘텐츠가 있습니다. 적용하시겠습니까?")
                                    .setTitle("정보 자동 채우기")
                                    .setPositiveButton("네") { dialog, id ->
                                        // Open Graph 를 통해 가져온 정보를 기반으로 레이아웃 적용
                                        binding.editTextItemName.setText(siteTitle)
                                        binding.editTextItemMemo.setText(siteDescription)
                                    }
                                    .setNegativeButton("아니요") { dialog, id ->
                                        /* no-op */
                                    }
                                builder.create().show()
                            }
                        }
                        override fun onBeforeLoading() {
                            /* no-op */
                        }
                    })
                }
                else ->{
                    binding.itemLinkLayout.setOnClickListener {
                        Toast.makeText(this, "올바르지 않은 URL 입니다", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // 가격을 입력하는 EditText 가, 가격 단위에 맞게 ',' 를 자동으로 삽입해주는 동작을 하기 위해 TextWatcher 붙여줌
        binding.editTextItemPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank() && s.toString() != formatPriceResult) {
                    formatPriceResult = decimalFormat.format(s.toString().replace(",".toRegex(), "").toDouble())
                    binding.editTextItemPrice.run{
                        setText(formatPriceResult)
                        setSelection(formatPriceResult.length)
                    }
                }
            }
        })

        // ImageView 를 눌렀을 때 이미지 추가 액티비티로 이동
        binding.imageViewItemImage.setOnClickListener {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setActivityTitle("이미지 추가")
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setCropMenuCropButtonTitle("완료")
                    .setRequestedSize(1280, 900)
                    .start(this)
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }

        // 작성 완료 버튼을 눌렀을 때
        binding.completeButton.setOnClickListener {
            itemName = binding.editTextItemName.text.toString().trim()
            val itemImage = itemImage.toString()  // Uri 를 String 으로 변환한 형태
            itemLink = binding.editTextItemLink.text.toString().trim()
            itemPrice = binding.editTextItemPrice.text.toString().trim()
            itemPrice = binding.editTextItemPrice.text.toString().replace(",", "")
            itemPriority = binding.ratingItemPriority.rating.toInt()
            itemMemo = binding.editTextItemMemo.text.toString().trim()

            // Valid Check
            // TODO : 코드가 비효율적으로 보이지만, 이렇게 해야 두 EditText 가 모두 비었을 때 둘 다 에러가 적용된다. (더 나은 방법 탐색 필요)
            //  setErrorOnEditText() 는 해당 EditText 에 특정 Error 를 뿌려줌
            if (itemName.isEmpty() || itemPrice.isEmpty()) {
                if (itemName.isEmpty()) {
                    setErrorOnEditText(
                        binding.editTextItemName,
                        resources.getText(R.string.itemNameError)
                    )
                }
                if (itemPrice.isEmpty()) {
                    setErrorOnEditText(
                        binding.editTextItemPrice,
                        resources.getText(R.string.itemPriceError)
                    )
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

                        // WorkerManager 는 커스텀 파라미터를 지원하지 않기 때문에 setInputData() 를 통해 데이터를 주입해야함
                        val inputData = Data.Builder()
                                .putString(ITEM_NAME, itemName)
                                .build()

                        // 하루에 한 번씩 구매를 유도하는 리마인드 푸시알림을 위해 NotificationWorker 를 WorkRequest 에 포함
                        val registerNotificationRequest =
                                PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
                                        .setInputData(inputData)
                                        .setInitialDelay(24, TimeUnit.HOURS)
                                        .addTag(itemName)  // WorkRequest 에 아이템 명으로 된 고유 태그 명시
                                        .build()

                        // 시스템에 WorkRequest 제출
                        WorkManager.getInstance(context).enqueue(registerNotificationRequest)

                        // 수정된 내용을 사용자에게 보여줌
                        val intent = Intent(context, ItemDetailActivity::class.java)
                        intent.putExtra(EXTRA_ITEM, newItem)

                        startActivity(intent)
                        finish()

                        // TODO("Item 을 Delete 했을 때 WorkManager 태스크를 캔슬할 수 있도록 구현해야 함")
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
     * 아이템 신규 생성이 아닌 편집 기능인 경우 기존 정보 채워주는 메소드
     */
    private fun applyExistingInfo(item: ItemEntity) {
        binding.textViewTitle.text = "수정하기"
        itemId = item.id
        binding.editTextItemName.setText(item.name)
        binding.editTextItemLink.setText(item.link)
        binding.editTextItemPrice.setText(item.price.toString())
        binding.ratingItemPriority.rating = item.priority.toFloat()
        binding.editTextItemMemo.setText(item.note)
        itemImage = Uri.parse(item.image)
        binding.imageViewItemImage.setImageURI(itemImage)
    }

    companion object {
        const val EDIT_ITEM: String = "EDIT_ITEM"
        const val AUTO_ITEM: String = "AUTO_ITEM"
    }
}
