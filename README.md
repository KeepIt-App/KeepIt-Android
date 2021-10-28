![목업2 복사본](https://user-images.githubusercontent.com/30336663/139281001-ee925bfa-e391-4556-9f3c-a597caad6d31.png)

# KeepIt!
다운로드 링크
- https://play.google.com/store/apps/details?id=com.haero_kim.pickmeup

# 💡 Topic

- **나만의 똑똑한 온라인 쇼핑 리마인더**

# 📝 Summary

실제로 제가 필요하다고 느껴서 제작하게 된 앱 입니다. 간혹 구매하기로 했던 물품을 까먹어 골치 아팠던 적이 많았는데 (기간 내 재료 구매, 세일을 놓치는 등), 이를 방지하기 위해 앱에 물품을 등록해두면 주기적으로 푸시 알림을 제공하여 사용자가 물품 구매를 잊지 않도록 도와주는 서비스입니다. 만약 쇼핑몰 링크를 복사해둔 채 앱을 키게 되면, 자동으로 물품을 인식하여 등록 페이지로 안내해주어 편의성을 강조했습니다.

# ⭐️ Key Function

- **구매할 물품**의 이름, 이미지, 가격, 메모, 중요도 등을 입력하여 **앱에 등록**
- 사용자가 등록해둔 물품을 **최신순, 중요도순, 가격순으로 정렬**하여 보여줌
- 사용자가 등록해둔 물품 키워드 검색 기능 제공 (RxJava Debounce 적용)
- 아직 구매하지 않은 물품에 대하여 **정기적으로 푸시알림 제공 (WorkManager)**
- **클립보드에 쇼핑몰 링크가 감지**되면, **물품 정보를 자동으로 채워줌 (OpenGraph 파싱)**

# 🛠 Tech Stack

`Kotlin`, `JetPack`, `AAC`, `DataBinding`, `ViewModel`, `LiveData`, `Room DB`, `Koin`

`Glide`, `RxJava`, `RxAndroid`, `RxBinding`, `WorkManager`, `OpenGraph`, `Timber`

# ⚙️ Architecture

- `MVVM`

# 🤚🏻 Part

- **개인 프로젝트 (기획, 개발, 디자인 등)**

# 🤔 Learned

- JetPack **`Room`** 로컬 데이터베이스 사용법을 알게 되었음.
- **`Koin`** 을 통해 DI 를 처음으로 적용해보며, **의존성 주입의 편리함**을 깨닫게 되었음.
- **`RxJava`** 를 통해 **`EditText`** 입력값을 **`Observable`** 로 받아 **쿼리 디바운싱 스킬**을 적용해보았음.
- **`WorkManager`** 를 활용하여 **특정 백그라운드 동작을 예약**하는 기능을 구현하는 방법을 알게 되었음.
- **`OpenGraph`* Tag 개념을 알게 되었고, 이를 파싱하여 **미리보기 기능**을 구현해볼 수 있었음.
