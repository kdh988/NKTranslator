# 🇰🇵↔🇰🇷 남북 언어 번역기 (Android App)

Kotlin + Jetpack Compose로 제작된 남한 표준어 ↔ 북한 문화어 번역 앱입니다.
Claude AI(Anthropic API)를 사용하여 번역합니다.

## 📦 프로젝트 구조

```
NKTranslator/
├── app/src/main/
│   ├── java/com/nktranslator/
│   │   ├── MainActivity.kt              # 앱 진입점
│   │   ├── network/
│   │   │   ├── AnthropicApiService.kt   # Retrofit API 클라이언트
│   │   │   └── AnthropicModels.kt       # 요청/응답 데이터 모델
│   │   └── ui/
│   │       ├── TranslatorViewModel.kt   # 상태 관리 & 번역 로직
│   │       └── TranslatorScreen.kt      # Jetpack Compose UI
│   ├── res/values/
│   │   ├── strings.xml
│   │   └── themes.xml
│   └── AndroidManifest.xml
├── build.gradle
└── settings.gradle
```

## 🚀 빌드 및 실행 방법

### 1. Android Studio에서 열기
1. Android Studio 최신 버전 설치
2. `File > Open` → `NKTranslator` 폴더 선택
3. Gradle sync 완료 대기

### 2. API 키 설정
앱을 실행한 후 우상단 🔑 아이콘을 눌러 Anthropic API 키를 입력하세요.
- API 키 발급: https://console.anthropic.com/

### 3. 빌드
```bash
./gradlew assembleDebug
```
APK 위치: `app/build/outputs/apk/debug/app-debug.apk`

## ✨ 기능
- **남→북**: 표준어를 북한 문화어로 번역
- **북→남**: 문화어를 표준어로 번역  
- **자동 감지**: AI가 자동으로 방향 감지 후 번역
- **예시 문장**: 탭 한 번으로 예시 불러오기
- **복사 버튼**: 번역 결과 클립보드 복사
- **전환 버튼**: 입/출력 텍스트 방향 전환

## 🛠 기술 스택
- Kotlin
- Jetpack Compose (UI)
- ViewModel + StateFlow (상태 관리)
- Retrofit + OkHttp (네트워크)
- Anthropic Claude API (번역 엔진)

## ⚠️ 주의사항
- API 키는 앱 내 메모리에만 저장됩니다 (앱 재시작 시 재입력 필요)
- 보안을 위해 프로덕션 배포 시 DataStore 암호화 추가를 권장합니다
- AI 번역이므로 참고용으로만 활용하세요
