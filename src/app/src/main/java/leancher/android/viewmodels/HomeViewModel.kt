package leancher.android.viewmodels

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.ui.core.Text
import leancher.android.domain.intents.LeancherIntent
import java.io.Serializable

data class SelectionOption(val text: String, val select: () -> Unit)
sealed class Block(internal open val step: LeancherIntent.Step) {
    data class Text(override val step: LeancherIntent.Step.Text) : Block(step) {
        val content = step.content
    }

    data class Result(
        override val step: LeancherIntent.Step.Getter,
        val renderer: @Composable () -> Unit
    ) : Block(step)

    data class Action(override val step: LeancherIntent.Step.Action) : Block(step)
    data class Message(override val step: LeancherIntent.Step.Message) : Block(step) {
        val content = step.content
    }
}

sealed class CurrentBlock {
    data class Input(val renderer: @Composable () -> Unit) : CurrentBlock()
    data class Selection(val options: List<SelectionOption>) : CurrentBlock()
}

typealias InputRenderer = @Composable (setResult: (Any) -> Unit) -> Unit
typealias OutputRenderer = @Composable () -> Unit

@Composable
fun NoRendererDefined() = Text("No Renderer defined")

class HomeViewModel(
    private val inputRenderers: Map<String, InputRenderer>,
    private val outputRenderers: Map<String, OutputRenderer>,
    private val actions: Actions
) : ViewModel() {
    private val intents = leancher.android.domain.intents.intents

    var blocks: List<Block> by mutableStateOf(listOf())
        private set
    var currentBlock: CurrentBlock? by mutableStateOf(null)
        private set
    private val steps get() = blocks.map { block -> block.step }
    private val stepIndex get() = steps.size
    private val stepIds get() = steps.map { step -> step.id }
    private val matchingIntents get() = intents.filter { intent -> intent.matches(stepIds) }
    private val nextSteps
        get() = matchingIntents.mapNotNull { intent ->
            intent.blocks.getOrNull(
                stepIndex
            )
        }

    private fun advanceWith(block: Block) {
        blocks = blocks + block // add the block to the already finished blocks
        advance()
    }

    private fun finish(step: LeancherIntent.Step.Text) {
        clearCurrentBlock()
        advanceWith(Block.Text(step))
    }
    private fun finish(step: LeancherIntent.Step.Getter) {
        clearCurrentBlock()
        advanceWith(Block.Result(
            step,
            outputRenderers[step.resultRenderer?.id] ?: { NoRendererDefined() }
        ))
    }
    private fun finish(step: LeancherIntent.Step.Action) = advanceWith(Block.Action(step))
    private fun finish(step: LeancherIntent.Step.Message) = advanceWith(Block.Message(step))

    fun start() = advance()

    private fun startOver() {
        values.reset()
        blocks = listOf()
        clearCurrentBlock()
    }

    private fun clearCurrentBlock() {
        currentBlock = null
    }

    private fun doStep(step: LeancherIntent.Step) = when (step) {
        is LeancherIntent.Step.Getter.InputGetter -> currentBlock = CurrentBlock.Input(
            inputRenderers[step.inputRenderer.id]?.let { renderer ->
                {
                    renderer.invoke { result ->
                        values.set(step.reference, result)
                        finish(step)
                    }
                }
            } ?: { NoRendererDefined() })
        is LeancherIntent.Step.Getter.IntentGetter -> actions.executeIntentForResult(
            createIntent(
                step.definition
            )
        ) { result ->
            values.set(step.reference, result)
            finish(step)
        }
        is LeancherIntent.Step.Action.LaunchIntentByReference -> {
            actions.executeIntent(values.get<Intent>(step.reference)!!)
            finish(step)
        }
        is LeancherIntent.Step.Action.LaunchIntentByDefinition -> {
            actions.executeIntent(createIntent(step.definition))
            finish(step)
        }
        is LeancherIntent.Step.Message -> finish(step)
        else -> throw Exception("maybe try exhaustive switches ;)")
    }

    private fun createSelection() {
        currentBlock = CurrentBlock.Selection(
            intents
                .filter { intent -> intent.matches(stepIds) }
                .mapNotNull { intent -> intent.blocks.elementAtOrNull(stepIndex) }
                .filterIsInstance<LeancherIntent.Step.Text>()
                .map { step -> SelectionOption(step.content) { finish(step) } })
    }

    private fun advance() {
        fun isIntentFinished() = matchingIntents.size == 1 && nextSteps.isEmpty()
        fun isSelectionNeeded() = nextSteps.size > 1
        when {
            isIntentFinished() -> startOver()
            isSelectionNeeded() -> createSelection()
            else -> doStep(nextSteps.first())
        }
    }

    private val values = object {
        private val values: MutableMap<String, Any> = mutableMapOf()
        fun set(reference: LeancherIntent.Value.Reference, value: Any) {
            values[reference.key] = value
        }

        fun <T> get(value: LeancherIntent.Value): T? =
            when (value) {
                is LeancherIntent.Value.Reference -> values[value.key]
                is LeancherIntent.Value.Constant -> value.value
            } as T?

        fun reset() = values.clear()
    }

    private fun createIntent(definition: LeancherIntent.IntentDefinition): Intent =
        Intent(definition.id).apply {
            when (definition.additional) {
                is LeancherIntent.IntentDefinition.Additional.Data ->
                    setDataAndNormalize(Uri.parse(values.get(definition.additional.content)))
                is LeancherIntent.IntentDefinition.Additional.Type ->
                    setTypeAndNormalize(values.get(definition.additional.content))
            }

            definition.extras?.forEach { definition ->
                putExtra(
                    definition.id,
                    values.get<Serializable>(definition.value)
                )
            }
        }

    data class Actions(
        val executeIntent: (Intent) -> Unit,
        val executeIntentForResult: ((Intent, ((Intent) -> Unit)) -> Unit),
        val isIntentCallable: (Intent) -> Boolean
    )
}