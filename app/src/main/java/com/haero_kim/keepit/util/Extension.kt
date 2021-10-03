package com.haero_kim.keepit.util

import android.view.View
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * EditText 에 RxJava (feat. RxBinding, RxKotlin) 을 적용하여
 * 사용자의 검색 Query 에 즉각적으로 LiveData 가 변경될 수 있도록 함 (Debounce 를 적용하여 쿼리 낭비 방지)
 */
fun EditText.setDebounceOnEditText(whenQueryChanged: (String) -> Unit): Disposable {
    val editTextChangeObservable = this.textChanges()
    val searchEditTextSubscription: Disposable =
        // 생성한 Observable 에 Operator 추가
        editTextChangeObservable
            // 마지막 글자 입력 0.8초 후에 onNext 이벤트로 데이터 스트리밍
            .debounce(800, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            // 구독을 통해 이벤트 응답 처리
            .subscribeBy(
                onNext = {
                    Timber.d("onNext : $it")
                    whenQueryChanged(it.toString())
                },
                onComplete = {
                    Timber.d("onComplete")
                },
                onError = {
                    Timber.i("onError : $it")
                }
            )
    return searchEditTextSubscription
}

fun <T, K, R> LiveData<T>.combineWith(
    liveData: LiveData<K>,
    block: (T?, K?) -> R
): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(this) {
        result.value = block(this.value, liveData.value)
    }
    result.addSource(liveData) {
        result.value = block(this.value, liveData.value)
    }
    return result
}
