package com.stormyai.app.presentation.generate

import com.stormyai.app.domain.model.*
import com.stormyai.app.domain.usecase.*
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
