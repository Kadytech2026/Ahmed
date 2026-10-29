package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.ImageProcessor
import com.example.model.ConversionResult
import com.example.model.ImageStats
import com.example.model.OutputFormat
import com.example.model.ResolutionPreset
import com.example.model.SavedImageItem
import com.example.model.SharpnessLevel
import com.example.model.StyleFilter
import com.example.repository.ImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConverterUiState(
    val selectedUri: Uri? = null,
    val originalStats: ImageStats? = null,
    val conversionResult: ConversionResult? = null,
    val isProcessing: Boolean = false,
    val processingProgress: Float = 0f,
    val processingMessage: String = "",
    val selectedPreset: ResolutionPreset = ResolutionPreset.FHD_1080P,
    val selectedSharpness: SharpnessLevel = SharpnessLevel.CRISP,
    val sharpnessIntensity: Int = 50,
    val smoothingIntensity: Int = 15,
    val quality: Int = 95,
    val selectedFormat: OutputFormat = OutputFormat.JPEG,
    val selectedFilter: StyleFilter = StyleFilter.NONE,
    val savedImages: List<SavedImageItem> = emptyList(),
    val lastSavedUri: Uri? = null,
    val userMessage: String? = null,
    val activeTab: Int = 0,
    val splitPosition: Float = 0.5f,
    val isAutoConverted: Boolean = false
)


class ConverterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ImageRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ConverterUiState())
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()

    init {
        loadSavedImages()
    }

    fun onImageSelected(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedUri = uri,
                    conversionResult = null,
                    lastSavedUri = null,
                    isProcessing = true,
                    processingMessage = "جاري قراءة تفاصيل الصورة…"
                )
            }
            try {
                val stats = ImageProcessor.decodeImageStats(getApplication(), uri)
                _uiState.update {
                    it.copy(
                        originalStats = stats,
                        isProcessing = false,
                        processingMessage = ""
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        processingMessage = "",
                        userMessage = "تعذر قراءة بيانات الصورة، يرجى تجربة صورة أخرى"
                    )
                }
            }
        }
    }

    fun selectSampleImage(drawableResId: Int, sampleName: String) {
        viewModelScope.launch {
            try {
                val app = getApplication<Application>()
                val cacheFile = java.io.File(app.cacheDir, "$sampleName.jpg")
                app.resources.openRawResource(drawableResId).use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val uri = Uri.fromFile(cacheFile)
                onImageSelected(uri)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(userMessage = "تعذر تحميل الصورة التجريبية: ${e.localizedMessage}")
                }
            }
        }
    }

    fun setPreset(preset: ResolutionPreset) {
        _uiState.update { it.copy(selectedPreset = preset) }
    }

    fun setSharpness(sharpness: SharpnessLevel) {
        _uiState.update { it.copy(selectedSharpness = sharpness) }
    }

    fun setSharpnessIntensity(intensity: Int) {
        _uiState.update { it.copy(sharpnessIntensity = intensity.coerceIn(0, 100)) }
    }

    fun setSmoothingIntensity(intensity: Int) {
        _uiState.update { it.copy(smoothingIntensity = intensity.coerceIn(0, 100)) }
    }

    fun setQuality(quality: Int) {
        _uiState.update { it.copy(quality = quality.coerceIn(50, 100)) }
    }

    fun setFormat(format: OutputFormat) {
        _uiState.update { it.copy(selectedFormat = format) }
    }

    fun setFilter(filter: StyleFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }

        // If an enhanced image already exists, instantly apply the filter to the base enhanced bitmap
        val currentResult = _uiState.value.conversionResult
        if (currentResult != null) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                try {
                    val baseBitmap = currentResult.baseEnhancedBitmap ?: currentResult.convertedBitmap
                    val newBitmap = ImageProcessor.applyStyleFilter(baseBitmap, filter)
                    val format = _uiState.value.selectedFormat
                    val quality = _uiState.value.quality
                    val newBytes = ImageProcessor.compressBitmap(newBitmap, format, quality)

                    _uiState.update { state ->
                        val updatedStats = state.conversionResult?.convertedStats?.copy(
                            byteSize = newBytes.size.toLong()
                        ) ?: ImageStats(newBitmap.width, newBitmap.height, newBytes.size.toLong())

                        state.copy(
                            lastSavedUri = null,
                            conversionResult = currentResult.copy(
                                convertedBitmap = newBitmap,
                                baseEnhancedBitmap = baseBitmap,
                                outputBytes = newBytes,
                                convertedStats = updatedStats,
                                appliedFilter = filter
                            ),
                            userMessage = "تم تطبيق فلتر «${filter.title}» فورياً!"
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(userMessage = "تعذر تطبيق الفلتر: ${e.localizedMessage}") }
                }
            }
        }
    }

    fun setActiveTab(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex) }
        if (tabIndex == 1) {
            loadSavedImages()
        }
    }

    fun setSplitPosition(pos: Float) {
        _uiState.update { it.copy(splitPosition = pos.coerceIn(0.05f, 0.95f)) }
    }

    fun applyCrop(
        normLeft: Float,
        normTop: Float,
        normRight: Float,
        normBottom: Float,
        rotationDegrees: Int = 0
    ) {
        val uri = _uiState.value.selectedUri ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, processingMessage = "جاري تطبيق القص وتحديد التركيز…") }
            try {
                val (_, croppedUri) = ImageProcessor.cropImage(
                    context = getApplication(),
                    inputUri = uri,
                    normLeft = normLeft,
                    normTop = normTop,
                    normRight = normRight,
                    normBottom = normBottom,
                    rotationDegrees = rotationDegrees
                )
                onImageSelected(croppedUri)
                _uiState.update { it.copy(userMessage = "تم قص الصورة وتحديد منطقة التركيز بنجاح!") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        userMessage = "حدث خطأ أثناء القص: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun convertImage() {
        val uri = _uiState.value.selectedUri ?: return
        val preset = _uiState.value.selectedPreset
        val quality = _uiState.value.quality
        val format = _uiState.value.selectedFormat
        val sharpness = _uiState.value.sharpnessIntensity / 100f
        val smoothing = _uiState.value.smoothingIntensity / 100f
        val filter = _uiState.value.selectedFilter

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    processingProgress = 0.05f,
                    processingMessage = "جاري بدء معالجة الصورة وتحسينها إلى ${preset.title}…"
                )
            }

            try {
                val result = ImageProcessor.processImage(
                    context = getApplication(),
                    inputUri = uri,
                    preset = preset,
                    sharpnessIntensity = sharpness,
                    smoothingIntensity = smoothing,
                    filter = filter,
                    quality = quality,
                    format = format,
                    onProgress = { progress, msg ->
                        _uiState.update {
                            it.copy(
                                processingProgress = progress,
                                processingMessage = msg
                            )
                        }
                    }
                )

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        processingProgress = 1.0f,
                        conversionResult = result,
                        processingMessage = "",
                        userMessage = "تم التحويل بنجاح في ${result.processingTimeMs} مللي ثانية!"
                    )
                }
            } catch (e: Exception) {

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        processingMessage = "",
                        userMessage = "حدث خطأ أثناء التحويل: ${e.localizedMessage ?: "حجم الصورة كبير جداً"}"
                    )
                }
            }
        }
    }

    fun saveConvertedImage() {
        val result = _uiState.value.conversionResult ?: return
        val format = _uiState.value.selectedFormat

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, processingMessage = "جاري الحفظ في المعرض…") }

            val saveResult = repository.saveImageToGallery(
                bytes = result.outputBytes,
                baseName = "KadyTech_${_uiState.value.selectedPreset.name}",
                format = format,
                width = result.convertedStats.width,
                height = result.convertedStats.height
            )

            saveResult.onSuccess { savedUri ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        processingMessage = "",
                        lastSavedUri = savedUri,
                        userMessage = "تم حفظ الصورة بنجاح في المعرض داخل مجلد Pictures/KADY TECH"
                    )
                }
                loadSavedImages()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        processingMessage = "",
                        userMessage = "تعذر حفظ الصورة: ${err.message}"
                    )
                }
            }
        }
    }

    fun shareConvertedImage() {
        val lastUri = _uiState.value.lastSavedUri
        if (lastUri != null) {
            repository.shareImage(lastUri, _uiState.value.selectedFormat.mimeType)
        } else {
            // Save first then share
            val result = _uiState.value.conversionResult ?: return
            viewModelScope.launch {
                val saveResult = repository.saveImageToGallery(
                    bytes = result.outputBytes,
                    baseName = "KadyTech_Share",
                    format = _uiState.value.selectedFormat
                )
                saveResult.onSuccess { savedUri ->
                    _uiState.update { it.copy(lastSavedUri = savedUri) }
                    repository.shareImage(savedUri, _uiState.value.selectedFormat.mimeType)
                }
            }
        }
    }

    fun reset() {
        _uiState.update {
            it.copy(
                selectedUri = null,
                originalStats = null,
                conversionResult = null,
                isProcessing = false,
                processingMessage = "",
                lastSavedUri = null,
                splitPosition = 0.5f
            )
        }
    }

    fun loadSavedImages() {
        viewModelScope.launch {
            val list = repository.getSavedKadyImages()
            _uiState.update { it.copy(savedImages = list) }
        }
    }

    fun viewSavedImage(item: SavedImageItem) {
        repository.viewImageInGallery(item.uri)
    }

    fun shareSavedImage(item: SavedImageItem) {
        repository.shareImage(item.uri)
    }

    fun deleteSavedImage(item: SavedImageItem) {
        viewModelScope.launch {
            val deleted = repository.deleteImage(item.uri)
            if (deleted) {
                loadSavedImages()
                _uiState.update { it.copy(userMessage = "تم حذف الصورة من المعرض") }
            }
        }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
