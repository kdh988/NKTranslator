package com.nktranslator.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// ── Color Palette ───────────────────────────────────────────────────
val DarkBg = Color(0xFF0D0D0D)
val SurfaceDark = Color(0xFF1A1A1A)
val SurfaceCard = Color(0xFF222222)
val BorderColor = Color(0xFF2E2E2E)
val AccentRed = Color(0xFFB40000)
val AccentBlue = Color(0xFF1E3C78)
val AccentGold = Color(0xFFE8C55A)
val TextPrimary = Color(0xFFF0EAD6)
val TextSecondary = Color(0xFF888888)
val TextMuted = Color(0xFF444444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(viewModel: TranslatorViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboardManager.current
    var showApiDialog by remember { mutableStateOf(false) }

    // API Key dialog
    if (showApiDialog) {
        ApiKeyDialog(
            currentKey = uiState.apiKey,
            onDismiss = { showApiDialog = false },
            onConfirm = { key ->
                viewModel.onApiKeyChanged(key)
                showApiDialog = false
            }
        )
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "남북 언어 번역기",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            "표준어 ↔ 문화어 · 조선어",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showApiDialog = true }) {
                        Icon(
                            Icons.Default.Key,
                            contentDescription = "API 키 설정",
                            tint = if (uiState.apiKey.isNotEmpty()) AccentGold else TextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Direction Selector ──────────────────────────────────
            DirectionSelector(
                selected = uiState.direction,
                onSelect = viewModel::onDirectionChanged
            )

            // ── Input Card ──────────────────────────────────────────
            TranslationCard(
                label = uiState.direction.fromLabel,
                labelColor = if (uiState.direction == TranslationDirection.NORTH_TO_SOUTH)
                    AccentGold else AccentRed,
                text = uiState.inputText,
                isEditable = true,
                placeholder = when (uiState.direction) {
                    TranslationDirection.SOUTH_TO_NORTH -> "남한 표준어를 입력하세요\n예: 스마트폰으로 셀카 찍었어"
                    TranslationDirection.NORTH_TO_SOUTH -> "북한 문화어를 입력하세요\n예: 손전화기로 자기사진 찍었다"
                    TranslationDirection.AUTO -> "번역할 텍스트를 입력하세요"
                },
                onTextChange = viewModel::onInputChanged,
                trailingAction = {
                    if (uiState.inputText.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onInputChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "지우기", tint = TextSecondary)
                        }
                    }
                }
            )

            // ── Swap / Translate Row ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Swap button
                OutlinedIconButton(
                    onClick = viewModel::swapTexts,
                    border = BorderStroke(1.dp, BorderColor),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = SurfaceCard
                    )
                ) {
                    Icon(Icons.Default.SwapVert, contentDescription = "전환", tint = TextSecondary)
                }

                Spacer(Modifier.width(12.dp))

                // Translate button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.translate()
                    },
                    enabled = uiState.inputText.isNotBlank() && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRed,
                        disabledContainerColor = SurfaceCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp).widthIn(min = 140.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = TextPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("번역 중...", color = TextPrimary, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Translate, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("번  역", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    }
                }
            }

            // ── Error message ───────────────────────────────────────
            AnimatedVisibility(visible = uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A0000)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp))
                        Text(uiState.errorMessage ?: "", color = Color(0xFFFF6B6B), fontSize = 13.sp)
                    }
                }
            }

            // ── Output Card ─────────────────────────────────────────
            TranslationCard(
                label = uiState.direction.toLabel,
                labelColor = if (uiState.direction == TranslationDirection.SOUTH_TO_NORTH)
                    AccentGold else Color(0xFF5A9EE8),
                text = uiState.outputText,
                isEditable = false,
                placeholder = "번역 결과가 여기에 표시됩니다",
                onTextChange = {},
                trailingAction = {
                    if (uiState.outputText.isNotEmpty()) {
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(uiState.outputText))
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "복사", tint = TextSecondary)
                        }
                    }
                }
            )

            // ── Example Phrases ─────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "예시 문장",
                    color = TextMuted,
                    fontSize = 11.sp,
                    letterSpacing = 3.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    itemsIndexed(EXAMPLE_PHRASES) { index, pair ->
                        val displayText = if (uiState.direction == TranslationDirection.NORTH_TO_SOUTH)
                            pair.second else pair.first
                        ExampleChip(text = displayText, onClick = { viewModel.loadExample(index) })
                    }
                }
            }

            // ── Footer ──────────────────────────────────────────────
            Text(
                "* AI 번역으로 참고용 제공 · 실제 북한 문화어와 차이가 있을 수 있습니다",
                color = Color(0xFF2A2A2A),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
        }
    }
}

// ── Direction Selector ───────────────────────────────────────────────
@Composable
fun DirectionSelector(
    selected: TranslationDirection,
    onSelect: (TranslationDirection) -> Unit
) {
    val options = TranslationDirection.values()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { dir ->
            val isSelected = dir == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) Brush.horizontalGradient(listOf(AccentRed, AccentBlue))
                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable { onSelect(dir) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    dir.label,
                    color = if (isSelected) Color.White else TextMuted,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── Translation Card ─────────────────────────────────────────────────
@Composable
fun TranslationCard(
    label: String,
    labelColor: Color,
    text: String,
    isEditable: Boolean,
    placeholder: String,
    onTextChange: (String) -> Unit,
    trailingAction: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    label,
                    color = labelColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                trailingAction()
            }

            Divider(color = BorderColor, thickness = 1.dp)

            // Text area
            if (isEditable) {
                BasicTextField(
                    text = text,
                    onValueChange = onTextChange,
                    placeholder = placeholder
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                        .padding(16.dp)
                ) {
                    if (text.isEmpty()) {
                        Text(placeholder, color = TextMuted, fontSize = 14.sp, lineHeight = 22.sp)
                    } else {
                        Text(
                            text,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BasicTextField(
    text: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (text.isEmpty()) {
            Text(placeholder, color = TextMuted, fontSize = 14.sp, lineHeight = 22.sp)
        }
        androidx.compose.foundation.text.BasicTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = TextPrimary,
                fontSize = 15.sp,
                lineHeight = 24.sp
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(AccentRed)
        )
    }
}

// ── Example Chip ─────────────────────────────────────────────────────
@Composable
fun ExampleChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text, color = TextSecondary, fontSize = 12.sp)
    }
}

// ── API Key Dialog ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyDialog(
    currentKey: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var keyInput by remember { mutableStateOf(currentKey) }
    var showKey by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        title = {
            Text("Anthropic API 키 설정", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "claude.ai에서 API 키를 발급받아 입력하세요.\n키는 기기에만 저장됩니다.",
                    color = TextSecondary, fontSize = 13.sp
                )
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    placeholder = { Text("sk-ant-...", color = TextMuted) },
                    visualTransformation = if (showKey) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentRed,
                        unfocusedBorderColor = BorderColor
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(keyInput.trim()) }) {
                Text("저장", color = AccentRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = TextSecondary)
            }
        }
    )
}
