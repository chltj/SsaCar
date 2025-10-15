
### 🚘 SSACAR — 카셰어링 앱 프로젝트

2025 MJC 모바일 앱 프로그래밍 실습 팀 프로젝트

</div>

  
<br>

## 📜 0. 목차
1. [프로젝트 소개](#1)
2. [팀원 소개](#2)
3. [개발 일정](#3)
4. [기술 스택](#4)
5. [브랜치 및 디렉토리 구조](#5)
6. [주요 기능 소개](#6)
7. [상세 담당 업무](#7)
8. [주요 기술 구현 및 도전 과제](#8)
9. [트러블 슈팅](#9)
10. [개선 목표](#10)
11. [프로젝트 후기](#11)

<br>

## 📝 <span id=1> 1. 프로젝트 소개</span>

SSACAR는 사용자가 차량을 손쉽게 대여·반납할 수 있는 모바일 카셰어링 플랫폼입니다.
기존 쏘카(SOCAR), 그린카와 같은 서비스를 참고하여, 로그인부터 차량 대여, 결제까지 모든 과정을 앱 하나로 구현하는 것을 목표로 개발했습니다.

* 사용자는 현재 위치 기반으로 가까운 주차장에서 차량을 선택하고 예약 가능

* 시간 및 기간 설정 기능을 통해 원하는 기간 대여 가능

* 결제 및 카드 등록 기능을 통해 예약 절차 간소화

* 토스트 알림 및 이용내역 조회 기능으로 사용자 경험 향상


<br><br>

## 🙋‍♂️ <span id=2>  2. 팀원 소개</span>

|학번|이름|개발|
|:------:|:---:|:---:|
|2022261067|<a href="https://github.com/chltj" target="_blank" rel="noopener noreferrer">최서연</a>|총괄, Front-End,Back-End|
|2021261009|<a href="https://github.com/ksw170" target="_blank" rel="noopener noreferrer">김상원</a>| Front-End,Back-End|
|2021261045|<a href="https://github.com/ysc0412" target="_blank" rel="noopener noreferrer">윤석찬</a>| Front-End,Back-End|
|2023261019|<a href="https://github.com/kim-doh-yun" target="_blank" rel="noopener noreferrer">김도현</a>| Front-End,Back-End|


<br><br>

## ⌛ <span id=3> 3. 개발 일정</span>

> 기획 및 설계: 2025.04.15 ~ 2025.04.28
(기능 명세서 및 화면 설계, 프로세스 구상)

> 개발 기간: 2025.04.29 ~ 2025.06.09

<br><br>

## 🛠️ <span id=4> 4. 기술 스택</span>
> 개발 환경: Android Studio (Java)

🖥️ Front-end

* Java (Android): Android Studio에서 메인 개발 언어로 사용하여 앱 UI 및 기능 구현
  
* XML Layout: 앱 화면의 구조 및 레이아웃 설계 (ConstraintLayout, LinearLayout 등 활용)
  * RecyclerView: 차량 리스트 및 알림 내역 리스트 UI 구현에 사용
  * Spinner & Date/Time Picker: 기간 및 시간 설정 기능 구현
  * Toast UI: 결제 및 알림 기능에서 실시간 피드백 메시지 표시
    
* Kakao Map API / Google Map API: 사용자 위치 기반 주차장 검색 및 핀 표시 기능 구현
    
* OkHttp / Retrofit: 외부 API 및 서버 통신 시 사용 (차량 정보 및 위치 데이터 연동)
* Glide: 차량 이미지 로딩 및 캐싱 처리

<br>

⚙️ Back-end

* Firebase Authentication: 회원가입 및 로그인 기능 구현 (ID/PW 기반 인증)

* Firebase Realtime Database: 사용자, 차량, 예약, 결제 데이터 실시간 저장 및 관리

* Firebase Storage: 이미지 또는 기타 데이터 저장 용도로 사용

* REST API: Retrofit을 통한 API 연동으로 차량 정보(현대 API) 불러오기

* OkHttpClient: 서버와의 통신 및 데이터 요청/응답 처리

* Java: 예약/결제 로직 및 화면 전환 제어 처리


<br>

🗄️ Database

* Firebase Realtime Database
  * 회원 정보, 면허 정보, 예약 내역, 차량 정보, 결제 내역 저장
  * JSON 구조 기반 실시간 데이터 반영
  * 예약 시 시간 중복 체크 및 데이터 검증 처리

* 현대 API (Hyundai Open API)
  * 차량 이미지, 이름, 연비 등 차량 정보 불러오기 및 리스트 반영

<br>

🚀 Build & Deployment

* Android Studio: 전체 앱 개발 및 빌드 환경
  
* Gradle: 의존성 관리 및 빌드 자동화

* GitHub: 버전 관리 및 팀원 협업

* APK 빌드 및 배포: Android 기기에 앱 설치 및 테스트 진행

<br><br>

## 🗂️ <span id="5"> 5. 브랜치 및 디렉토리 구조</span>

> 브랜치

- 'main' : 프로젝트 진행 중 항상 최신 상태로 유지하며, Stable 상태로 배포되어 바로 사용 가능한 브랜치입니다.

- 'seoyeon', 'credit', 'Kimdohyun', '윤석찬' : 개발 팀원들의 브랜치입니다.<br>각자가 맡은 기능 Test가 해당 각 브랜치에서 진행되며, 최종 Stable 버전을 Merge하여 main 브랜치에 푸시합니다.

<br>

> 디렉토리 구조

<br>

<details> <summary>📂 전체 프로젝트 구조 보기 (클릭하여 펼치기)</summary>

```text
📦main
 ┣ 📜AndroidManifest.xml
 ┣ 📂java
 ┃ ┗ 📂com
 ┃ ┃ ┗ 📂example
 ┃ ┃ ┃ ┃ ┗ 📂ssacar
 ┃ ┃ ┃ ┃ ┃ ┣ 📂activity
 ┃ ┃ ┃ ┃ ┃ ┣ 📜SplashActivity.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜LoginActivity.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜RegisterActivity.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜LicenseRegisterActivity.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜MainActivity.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜MapCallHereActivity.java          # 여기로 부르기
 ┃ ┃ ┃ ┃ ┃ ┣ 📜MapPickupActivity.java            # 가지러 가기 / 편도
 ┃ ┃ ┃ ┃ ┃ ┣ 📜TimeSettingActivity.java          # 시간/기간 설정
 ┃ ┃ ┃ ┃ ┃ ┣ 📜CarListActivity.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜CarDetailActivity.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜PaymentActivity.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜CardListActivity.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜CardAddActivity.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜HistoryActivity.java              # 이용내역
 ┃ ┃ ┃ ┃ ┃ ┗ 📜NotificationActivity.java         # 알림
 ┃ ┃ ┃ ┃ ┣ 📂adapter
 ┃ ┃ ┃ ┃ ┃ ┣ 📜CarListAdapter.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜HistoryListAdapter.java
 ┃ ┃ ┃ ┃ ┃ ┗ 📜NotificationListAdapter.java
 ┃ ┃ ┃ ┃ ┣ 📂model
 ┃ ┃ ┃ ┃ ┃ ┣ 📜User.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜License.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜ParkingLot.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜Car.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜Reservation.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜Payment.java
 ┃ ┃ ┃ ┃ ┃ ┗ 📜Notice.java
 ┃ ┃ ┃ ┃ ┣ 📂api
 ┃ ┃ ┃ ┃ ┃ ┣ 📜HyundaiApiService.java            # 현대 API 연동
 ┃ ┃ ┃ ┃ ┃ ┣ 📜MapApiService.java                # Kakao/Google Map
 ┃ ┃ ┃ ┃ ┃ ┣ 📜ApiClient.java                    # Retrofit/OkHttp 클라이언트
 ┃ ┃ ┃ ┃ ┃ ┗ 📜ApiInterceptor.java
 ┃ ┃ ┃ ┃ ┣ 📂firebase
 ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthManager.java                  # Firebase Auth 래퍼
 ┃ ┃ ┃ ┃ ┃ ┣ 📜DbManager.java                    # Realtime DB 접근
 ┃ ┃ ┃ ┃ ┃ ┗ 📜StorageManager.java               # (이미지 등) Storage
 ┃ ┃ ┃ ┃ ┣ 📂utils
 ┃ ┃ ┃ ┃ ┃ ┣ 📜TimeUtils.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜FormatUtils.java
 ┃ ┃ ┃ ┃ ┃ ┣ 📜Constants.java
 ┃ ┃ ┃ ┃ ┃ ┗ 📜PrefHelper.java                   # SharedPreferences
 ┃ ┃ ┃ ┃ ┗ 📂repository
 ┃ ┃ ┃ ┃ ┣ 📜CarRepository.java
 ┃  ┃ ┃ ┃ ┣ 📜ReservationRepository.java
 ┃         ┗ 📜PaymentRepository.java
 ┣ 📂res
 ┃ ┣ 📂layout
 ┃ ┃ ┣ 📜activity_splash.xml
 ┃ ┃ ┣ 📜activity_login.xml
 ┃ ┃ ┣ 📜activity_register.xml
 ┃ ┃ ┣ 📜activity_license_register.xml
 ┃ ┃ ┣ 📜activity_main.xml
 ┃ ┃ ┣ 📜activity_map_call_here.xml
 ┃ ┃ ┣ 📜activity_map_pickup.xml
 ┃ ┃ ┣ 📜activity_time_setting.xml
 ┃ ┃ ┣ 📜activity_car_list.xml
 ┃ ┃ ┣ 📜item_car.xml
 ┃ ┃ ┣ 📜activity_car_detail.xml
 ┃ ┃ ┣ 📜activity_payment.xml
 ┃ ┃ ┣ 📜activity_card_list.xml
 ┃ ┃ ┣ 📜activity_card_add.xml
 ┃ ┃ ┣ 📜activity_history.xml
 ┃ ┃ ┣ 📜item_history.xml
 ┃ ┃ ┣ 📜activity_notification.xml
 ┃ ┃ ┗ 📜item_notification.xml
 ┃ ┣ 📂drawable
 ┃ ┃ ┣ 📜bg_button.xml
 ┃ ┃ ┣ 📜shape_card.xml
 ┃ ┃ ┗ 📜selector_btn_primary.xml
 ┃ ┣ 📂mipmap-anydpi-v26
 ┃ ┃ ┗ 📜ic_launcher.xml
 ┃ ┣ 📂mipmap-hdpi
 ┃ ┣ 📂mipmap-mdpi
 ┃ ┣ 📂mipmap-xhdpi
 ┃ ┣ 📂mipmap-xxhdpi
 ┃ ┣ 📂mipmap-xxxhdpi
 ┃ ┣ 📂values
 ┃ ┃ ┣ 📜colors.xml
 ┃ ┃ ┣ 📜strings.xml
 ┃ ┃ ┣ 📜themes.xml
 ┃ ┃ ┗ 📜styles.xml
 ┃ ┣ 📂values-night
 ┃ ┃ ┗ 📜themes.xml
 ┃ ┗ 📂xml
 ┃   ┗ 📜network_security_config.xml
 ┗ 📂assets
   ┗ 📜mock_parkinglots.json                  # (선택) 목데이터
```
</details>

<br> <br>

## 💻 <span id="6"> 6. 주요 기능 소개</span>
<br><br>
해당 사이트를 실제로 구동해보기 위한 번거로운 작업(DB 설정, 라이브러리 설치, 서버 설치 및 실행)이 너무나 많기에 사이트 주요 기능들에 대해서<br>gif 형식으로 첨부하여 소개합니다.


## 🚀 주요 기능 소개

### 1. 로그인 화면
<img src="./screenshots/로그인.png" alt="로그인 화면" width="300px">
<br><br>

### 2. 지도 화면
<img src="./screenshots/구글맵지도.png" alt="지도 화면" width="300px">
<br><br>



<br><br>

