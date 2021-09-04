package com.haero_kim.pickmeup.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.work.Data
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.anandwana001.ogtagparser.LinkSourceContent
import com.anandwana001.ogtagparser.LinkViewCallback
import com.anandwana001.ogtagparser.OgTagParser
import com.bumptech.glide.Glide
import com.haero_kim.pickmeup.R
import com.haero_kim.pickmeup.base.BaseActivity
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.databinding.ActivityAddItemBinding
import com.haero_kim.pickmeup.ui.ItemDetailActivity.Companion.EXTRA_ITEM
import com.haero_kim.pickmeup.util.Util.Companion.setErrorOnEditText
import com.haero_kim.pickmeup.worker.NotificationWorker
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class AddItemActivity : BaseActivity<ActivityAddItemBinding, ItemViewModel>() {
    override val layoutResourceId: Int
        get() = R.layout.activity_add_item
    override val viewModel: ItemViewModel by viewModel()

    var itemId: Long? = null
    private var itemImage: Uri? = null

    // 물건의 가격을 입력하는 EditText 에 화폐 단위 표시를 하기위한 DecimalFormat
    private val decimalFormat = DecimalFormat("#,###")
    private var formatPriceResult = ""

    override fun initStartView() {
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
    }

    override fun initDataBinding() {
        viewModel.itemAddComplete.observe(this) {
            val item = it.getContentIfNotHandled()
            // WorkerManager 는 커스텀 파라미터를 지원하지 않기 때문에 setInputData() 를 통해 데이터를 주입해야함
            val inputData = Data.Builder()
                .putString(NotificationWorker.ITEM_NAME, item?.name)
                .build()

            // 하루에 한 번씩 구매를 유도하는 리마인드 푸시알림을 위해 NotificationWorker 를 WorkRequest 에 포함
            val registerNotificationRequest =
                PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
                    .setInputData(inputData)
                    .setInitialDelay(24, TimeUnit.HOURS)
                    .addTag(item!!.name)  // WorkRequest 에 아이템 명으로 된 고유 태그 명시
                    .build()

            // 시스템에 WorkRequest 제출
            WorkManager.getInstance(this).enqueue(registerNotificationRequest)

            // 수정된 내용을 사용자에게 보여줌
            val intent = Intent(this, ItemDetailActivity::class.java)
            intent.putExtra(EXTRA_ITEM, item)

            startActivity(intent)
            finish()

            // TODO("Item 을 Delete 했을 때 WorkManager 태스크를 캔슬할 수 있도록 구현해야 함")
        }

    }

    override fun initAfterBinding() {
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
            applyAutoFillForm()
        }

        // 가격을 입력하는 EditText 가, 가격 단위에 맞게 ',' 를 자동으로 삽입해주는 동작을 하기 위해 TextWatcher 붙여줌
        binding.editTextItemPrice.addTextChangedListener(applyPriceFormat)

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
            checkFormAndAddItem()
        }
    }

    /**
     * 화폐 단위를 자동으로 매겨주는 TextWatcher
     */
    private val applyPriceFormat = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (!s.isNullOrBlank() && s.toString() != formatPriceResult) {
                formatPriceResult =
                    decimalFormat.format(s.toString().replace(",".toRegex(), "").toDouble())
                binding.editTextItemPrice.run {
                    setText(formatPriceResult)
                    setSelection(formatPriceResult.length)
                }
            }
        }
    }


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

    private fun applyAutoFillForm() {
        var itemLink = intent.getStringExtra(AUTO_ITEM) ?: ""
        binding.editTextItemLink.setText(itemLink)
        when {
            Patterns.WEB_URL.matcher(itemLink).matches() -> {
                // 만약 http 형식이 아니라면 앞에 'http://' 를 붙여줘야함 ==> ex) www.naver.com 과 같은 상황
                if (!URLUtil.isHttpsUrl(itemLink) and !URLUtil.isHttpUrl(itemLink)) {
                    itemLink = "https://" + itemLink
                }
                // Open Graph 태그를 불러오는 라이브러리 사용
                OgTagParser().execute(itemLink, object : LinkViewCallback {
                    override fun onAfterLoading(linkSourceContent: LinkSourceContent) {
                        val siteTitle = linkSourceContent.ogTitle
                        val siteDescription = linkSourceContent.ogDescription
                        var siteThumbnail = linkSourceContent.images

                        if (siteTitle.isNotBlank() or siteDescription.isNotBlank() or siteThumbnail.isNotBlank()) {
                            if (siteThumbnail.startsWith("//")) {
                                siteThumbnail = "https:" + siteThumbnail
                            }

                            val builder = AlertDialog.Builder(this@AddItemActivity)
                            builder.setMessage("사이트에서 발견한 콘텐츠가 있습니다. 적용하시겠습니까?")
                                .setTitle("정보 자동 채우기")
                                .setPositiveButton("네") { dialog, id ->
                                    // Open Graph 를 통해 가져온 정보를 기반으로 레이아웃 적용
                                    binding.editTextItemName.setText(siteTitle)
                                    binding.editTextItemMemo.setText(siteDescription)

                                    viewModel.itemName.postValue(siteTitle)
                                    viewModel.itemMemo.postValue(siteDescription)
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
            else -> {
                binding.itemLinkLayout.setOnClickListener {
                    Toast.makeText(this, "올바르지 않은 URL 입니다", Toast.LENGTH_LONG).show()
                }
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

    private fun checkFormAndAddItem() {
        val itemImage = itemImage.toString()  // Uri 를 String 으로 변환한 형태
        // Valid Check
        when {
            viewModel.itemName.value!!.isEmpty() -> {
                setErrorOnEditText(
                    binding.editTextItemName,
                    resources.getText(R.string.itemNameError)
                )
            }
            viewModel.itemPrice.value!!.isEmpty() -> {
                setErrorOnEditText(
                    binding.editTextItemPrice,
                    resources.getText(R.string.itemPriceError)
                )
            }
            else -> {
                val builder = AlertDialog.Builder(this)
                builder.apply {
                    this.setMessage(resources.getText(R.string.completeDialog))
                    this.setNegativeButton("NO") { _, _ -> }
                    this.setPositiveButton("YES") { _, _ ->
                        viewModel.addItem(itemImage)
                    }
                }
                builder.show()
            }
        }
    }

    /**
     * 아이템 신규 생성이 아닌 편집 기능인 경우 기존 정보 채워주는 메소드
     */
    private fun applyExistingInfo(item: ItemEntity) {
        itemImage = Uri.parse(item.image)
        Glide.with(this)
            .load(item.image)
            .into(binding.imageViewItemImage)

        itemId = item.id
        binding.textViewTitle.text = "수정하기"

        viewModel.itemName.postValue(item.name)
        viewModel.itemLink.postValue(item.link)
        viewModel.itemPrice.postValue(item.price.toString())
        viewModel.itemPriority.postValue(item.priority.toFloat())
        viewModel.itemMemo.postValue(item.memo)
    }

    companion object {
        const val EDIT_ITEM: String = "EDIT_ITEM"
        const val AUTO_ITEM: String = "AUTO_ITEM"
    }
}
