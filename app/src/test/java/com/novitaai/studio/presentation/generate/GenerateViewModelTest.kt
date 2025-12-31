package com.novitaai.studio.presentation.generate

import com.novitaai.studio.domain.model.*
import com.novitaai.studio.domain.usecase.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GenerateViewModelTest {

    @MockK
    private lateinit var createImageUseCase: CreateImageUseCase

    @MockK
    private lateinit var pollTaskStatusUseCase: PollTaskStatusUseCase

    @MockK
    private lateinit var saveToHistoryUseCase: SaveToHistoryUseCase

    @MockK
    private lateinit var getModelsUseCase: GetModelsUseCase

    private lateinit var viewModel: GenerateViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        Dispatchers.setMain(testDispatcher)

        // Setup default mocks
        every { getModelsUseCase() } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads models`() = runTest {
        // Given
        val models = listOf(
            AiModel("model1", "Model 1", ModelType.IMAGE_GENERATION, true, true),
            AiModel("model2", "Model 2", ModelType.IMAGE_GENERATION, false, false)
        )
        every { getModelsUseCase() } returns Result.success(models)

        // When
        viewModel = GenerateViewModel(
            createImageUseCase,
            pollTaskStatusUseCase,
            saveToHistoryUseCase,
            getModelsUseCase
        )
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.models.size)
        assertNotNull(state.selectedModel)
        assertFalse(state.isLoading)
    }

    @Test
    fun `updatePrompt updates state correctly`() = runTest {
        // Given
        viewModel = GenerateViewModel(
            createImageUseCase,
            pollTaskStatusUseCase,
            saveToHistoryUseCase,
            getModelsUseCase
        )
        advanceUntilIdle()

        // When
        viewModel.updatePrompt("A beautiful landscape")

        // Then
        assertEquals("A beautiful landscape", viewModel.uiState.value.prompt)
    }

    @Test
    fun `updateGenerationType updates state correctly`() = runTest {
        // Given
        viewModel = GenerateViewModel(
            createImageUseCase,
            pollTaskStatusUseCase,
            saveToHistoryUseCase,
            getModelsUseCase
        )
        advanceUntilIdle()

        // When
        viewModel.updateGenerationType(GenerationType.TEXT_TO_VIDEO)

        // Then
        assertEquals(GenerationType.TEXT_TO_VIDEO, viewModel.uiState.value.generationType)
    }

    @Test
    fun `generate shows error when prompt is empty`() = runTest {
        // Given
        viewModel = GenerateViewModel(
            createImageUseCase,
            pollTaskStatusUseCase,
            saveToHistoryUseCase,
            getModelsUseCase
        )
        advanceUntilIdle()

        // When
        viewModel.generate()

        // Then
        assertEquals("Please enter a prompt", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isGenerating)
    }

    @Test
    fun `generate shows error when model is not selected`() = runTest {
        // Given
        viewModel = GenerateViewModel(
            createImageUseCase,
            pollTaskStatusUseCase,
            saveToHistoryUseCase,
            getModelsUseCase
        )
        advanceUntilIdle()

        viewModel.updatePrompt("A beautiful landscape")

        // Mock models to return empty
        every { getModelsUseCase() } returns Result.success(emptyList())

        // When
        viewModel.generate()

        // Then
        assertEquals("Please select a model", viewModel.uiState.value.error)
    }

    @Test
    fun `generate starts generation process`() = runTest {
        // Given
        val models = listOf(
            AiModel("meinamix_v11", "MeinaMix", ModelType.IMAGE_GENERATION, true, true)
        )
        every { getModelsUseCase() } returns Result.success(models)

        val expectedResult = GenerationResult(
            taskId = "task_123",
            type = GenerationType.TEXT_TO_IMAGE,
            status = TaskStatus.PENDING
        )

        coEvery { createImageUseCase(any(), any(), any(), any(), any(), any(), any()) } returns Result.success(expectedResult)
        coEvery { pollTaskStatusUseCase(any()) } returns Result.failure(Exception("Still processing"))

        viewModel = GenerateViewModel(
            createImageUseCase,
            pollTaskStatusUseCase,
            saveToHistoryUseCase,
            getModelsUseCase
        )
        advanceUntilIdle()

        viewModel.updatePrompt("A beautiful landscape")

        // When
        viewModel.generate()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.isGenerating)
        coVerify { createImageUseCase(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `toggleAdvancedSettings toggles correctly`() = runTest {
        // Given
        viewModel = GenerateViewModel(
            createImageUseCase,
            pollTaskStatusUseToHistoryUseCaseCase,
            save,
            getModelsUseCase
        )
        advanceUntilIdle()

        // When
        viewModel.toggleAdvancedSettings()

        // Then
        assertTrue(viewModel.uiState.value.showAdvancedSettings)

        // When
        viewModel.toggleAdvancedSettings()

        // Then
        assertFalse(viewModel.uiState.value.showAdvancedSettings)
    }

    @Test
    fun `updateWidth clamps value within range`() = runTest {
        // Given
        viewModel = GenerateViewModel(
            createImageUseCase,
            pollTaskStatusUseCase,
            saveToHistoryUseCase,
            getModelsUseCase
        )
        advanceUntilIdle()

        // When
        viewModel.updateWidth(3000)

        // Then
        assertEquals(2048, viewModel.uiState.value.width)
    }
}
