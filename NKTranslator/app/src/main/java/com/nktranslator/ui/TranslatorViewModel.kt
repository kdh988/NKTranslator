package com.nktranslator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nktranslator.network.AnthropicClient
import com.nktranslator.network.AnthropicRequest
import com.nktranslator.network.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TranslationDirection(val label: String, val fromLabel: String, val toLabel: String) {
    SOUTH_TO_NORTH("남 → 북", "표준어 (남한)", "문화어 (북한)"),
    NORTH_TO_SOUTH("북 → 남", "문화어 (북한)", "표준어 (남한)"),
    AUTO("자동 감지", "입력", "번역 결과")
}

data class TranslatorUiState(
    val inputText: String = "",
    val outputText: String = "",
    val direction: TranslationDirection = TranslationDirection.SOUTH_TO_NORTH,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val apiKey: String = ""
)

val EXAMPLE_PHRASES = listOf(
    Pair("스마트폰으로 셀카 찍었어", "손전화기로 자기사진 찍었다"),
    Pair("편의점에서 아이스크림 샀어", "봉사상점에서 얼음보숭이 샀다"),
    Pair("아버지 생일 파티 준비해", "아바이 생일잔치 준비해라"),
    Pair("지하철 타고 회사 갔어", "지하철도 타고 직장에 갔다"),
    Pair("컴퓨터로 영화 봤어", "콤퓨터로 예술영화 봤다")
)

private const val SYSTEM_PROMPT = """당신은 남한 표준어와 북한 문화어(조선어) 간의 전문 번역가입니다.

사용자가 입력한 텍스트를 지정된 방향으로 번역하세요.
번역 결과만 출력하세요. 설명이나 부연은 금지입니다.

북한 문화어 특징:
- 외래어를 고유어로 대체: 스마트폰→손전화기, 아이스크림→얼음보숭이, 텔레비전→텔레비죤, 컴퓨터→콤퓨터, 버스→뻐스, 피자→밀가루빵
- 두음법칙 미적용: 여자→녀자, 노동→로동, 역사→력사, 이유→리유
- 특유 어휘: 아버지→아바이, 어머니→어마니, 편의점→봉사상점, 영화→예술영화, 자전거→자전거
- 문체: ~이다, ~다 선호, "참으로", "실로" 등 강조 부사 사용"""

class TranslatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TranslatorUiState())
    val uiState: StateFlow<TranslatorUiState> = _uiState.asStateFlow()

    fun onInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text, errorMessage = null)
    }

    fun onApiKeyChanged(key: String) {
        _uiState.value = _uiState.value.copy(apiKey = key)
    }

    fun onDirectionChanged(direction: TranslationDirection) {
        _uiState.value = _uiState.value.copy(
            direction = direction,
            outputText = "",
            errorMessage = null
        )
    }

    fun swapTexts() {
        val current = _uiState.value
        val newDirection = when (current.direction) {
            TranslationDirection.SOUTH_TO_NORTH -> TranslationDirection.NORTH_TO_SOUTH
            TranslationDirection.NORTH_TO_SOUTH -> TranslationDirection.SOUTH_TO_NORTH
            TranslationDirection.AUTO -> TranslationDirection.AUTO
        }
        _uiState.value = current.copy(
            inputText = current.outputText,
            outputText = current.inputText,
            direction = newDirection
        )
    }

    fun loadExample(index: Int) {
        val pair = EXAMPLE_PHRASES[index]
        val text = if (_uiState.value.direction == TranslationDirection.NORTH_TO_SOUTH)
            pair.second else pair.first
        _uiState.value = _uiState.value.copy(inputText = text, outputText = "", errorMessage = null)
    }

    fun translate() {
        val state = _uiState.value
        if (state.inputText.isBlank()) return
        if (state.apiKey.isBlank()) {
            _uiState.value = state.copy(errorMessage = "API 키를 먼저 입력해주세요.")
            return
        }

        val directionInstruction = when (state.direction) {
            TranslationDirection.SOUTH_TO_NORTH -> "다음 남한 표준어를 북한 문화어로 번역하세요:\n\n${state.inputText}"
            TranslationDirection.NORTH_TO_SOUTH -> "다음 북한 문화어를 남한 표준어로 번역하세요:\n\n${state.inputText}"
            TranslationDirection.AUTO -> "다음 텍스트를 자동으로 감지하여 반대 방향으로 번역하세요:\n\n${state.inputText}"
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null, outputText = "")
            try {
                val response = AnthropicClient.service.translate(
                    apiKey = state.apiKey,
                    request = AnthropicRequest(
                        system = SYSTEM_PROMPT,
                        messages = listOf(Message(role = "user", content = directionInstruction))
                    )
                )
                if (response.isSuccessful) {
                    val result = response.body()?.content?.firstOrNull()?.text
                        ?: "번역 결과를 가져올 수 없습니다."
                    _uiState.value = _uiState.value.copy(outputText = result, isLoading = false)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "알 수 없는 오류"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "오류 ${response.code()}: API 키를 확인해주세요."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "네트워크 오류: ${e.localizedMessage ?: "연결을 확인해주세요."}"
                )
            }
        }
    }
}
