package com.novitaai.studio.domain.usecase

import com.novitaai.studio.domain.model.*
import com.novitaai.studio.domain.repository.GenerationRepository
import com.novitaai.studio.domain.repository.HistoryRepository
import com.novitaai.studio.domain.repository.SettingsRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CreateImageUseCaseTest {

    @MockK
    private lateinit var generationRepository: GenerationRepository

    @MockK
    private lateinit var settingsRepository: SettingsRepository

    @MockK
    private lateinit var historyRepository: HistoryRepository

    private lateinit var createImageUseCase: CreateImageUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        createImageUseCase = CreateImageUseCase(
            generationRepository,
            settingsRepository,
            historyRepository
        )
    }

    @Test
    fun `createImage returns success when API call succeeds`() = runTest {
        // Given
        val expectedResult = GenerationResult(
            taskId = "test_task_id",
            type = GenerationType.TEXT_TO_IMAGE,
            status = TaskStatus.PENDING,
            imageUrl = "https://example.com/image.png"
        )

        every { settingsRepository.getSettings() } returns flowOf(UserSettings())
        coEvery { generationRepository.generateImage(any()) } returns Result.success(expectedResult)

        // When
        val result = createImageUseCase(
            prompt = "A beautiful sunset",
            negativePrompt = "blurry",
            width = 512,
            height = 768
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
        coVerify { generationRepository.generateImage(match { task ->
            task.prompt == "A beautiful sunset" &&
            task.negativePrompt == "blurry" &&
            task.width == 512 &&
            task.height == 768
        }) }
    }

    @Test
    fun `createImage returns failure when API call fails`() = runTest {
        // Given
        val exception = Exception("API Error")
        every { settingsRepository.getSettings() } returns flowOf(UserSettings())
        coEvery { generationRepository.generateImage(any()) } returns Result.failure(exception)

        // When
        val result = createImageUseCase(prompt = "Test prompt")

        // Then
        assertTrue(result.isFailure)
        assertEquals("API Error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createImage uses default settings when not provided`() = runTest {
        // Given
        val expectedResult = GenerationResult(
            taskId = "test_task_id",
            type = GenerationType.TEXT_TO_IMAGE,
            status = TaskStatus.PENDING
        )

        val defaultSettings = UserSettings(
            defaultModel = "meinamix_v11",
            defaultWidth = 512,
            defaultHeight = 768,
            defaultSteps = 25,
            defaultCfgScale = 7.0f
        )

        every { settingsRepository.getSettings() } returns flowOf(defaultSettings)
        coEvery { generationRepository.generateImage(any()) } returns Result.success(expectedResult)

        // When
        val result = createImageUseCase(prompt = "Test prompt")

        // Then
        assertTrue(result.isSuccess)
        coVerify { generationRepository.generateImage(match { task ->
            task.modelName == "meinamix_v11" &&
            task.width == 512 &&
            task.height == 768 &&
            task.steps == 25 &&
            task.cfgScale == 7.0f
        }) }
    }
}

class SaveToHistoryUseCaseTest {

    @MockK
    private lateinit var historyRepository: HistoryRepository

    private lateinit var saveToHistoryUseCase: SaveToHistoryUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        saveToHistoryUseCase = SaveToHistoryUseCase(historyRepository)
    }

    @Test
    fun `saveToHistory saves item and returns id`() = runTest {
        // Given
        coEvery { historyRepository.saveHistoryItem(any()) } returns 1L

        // When
        val result = saveToHistoryUseCase(
            taskId = "task_123",
            type = GenerationType.TEXT_TO_IMAGE,
            prompt = "Test prompt",
            resultUrl = "https://example.com/image.png",
            modelName = "meinamix_v11"
        )

        // Then
        assertEquals(1L, result)
        coVerify {
            historyRepository.saveHistoryItem(match { item ->
                item.taskId == "task_123" &&
                item.type == GenerationType.TEXT_TO_IMAGE &&
                item.prompt == "Test prompt" &&
                item.resultUrl == "https://example.com/image.png" &&
                item.modelName == "meinamix_v11"
            })
        }
    }
}

class GetHistoryUseCaseTest {

    @MockK
    private lateinit var historyRepository: HistoryRepository

    private lateinit var getHistoryUseCase: GetHistoryUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        getHistoryUseCase = GetHistoryUseCase(historyRepository)
    }

    @Test
    fun `getHistory returns all history items`() = runTest {
        // Given
        val items = listOf(
            HistoryItem(
                id = 1,
                taskId = "task_1",
                type = GenerationType.TEXT_TO_IMAGE,
                prompt = "Prompt 1",
                thumbnailUrl = "url1",
                resultUrl = "url1",
                modelName = "model1",
                createdAt = 1000L
            ),
            HistoryItem(
                id = 2,
                taskId = "task_2",
                type = GenerationType.TEXT_TO_VIDEO,
                prompt = "Prompt 2",
                thumbnailUrl = "url2",
                resultUrl = "url2",
                modelName = "model2",
                createdAt = 2000L
            )
        )

        every { historyRepository.getAllHistory() } returns flowOf(items)

        // When
        val flow = getHistoryUseCase()

        // Then
        flow.collect { result ->
            assertEquals(2, result.size)
            assertEquals("task_1", result[0].taskId)
            assertEquals("task_2", result[1].taskId)
        }
    }

    @Test
    fun `getHistory byType filters correctly`() = runTest {
        // Given
        val imageItems = listOf(
            HistoryItem(
                id = 1,
                taskId = "task_1",
                type = GenerationType.TEXT_TO_IMAGE,
                prompt = "Prompt 1",
                thumbnailUrl = "url1",
                resultUrl = "url1",
                modelName = "model1",
                createdAt = 1000L
            )
        )

        every { historyRepository.getHistoryByType(GenerationType.TEXT_TO_IMAGE) } returns flowOf(imageItems)

        // When
        val flow = getHistoryUseCase.byType(GenerationType.TEXT_TO_IMAGE)

        // Then
        flow.collect { result ->
            assertEquals(1, result.size)
            assertEquals(GenerationType.TEXT_TO_IMAGE, result[0].type)
        }
    }
}
