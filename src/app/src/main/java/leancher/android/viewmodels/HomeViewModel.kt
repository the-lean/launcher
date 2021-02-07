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

class HomeViewModel(
    inputRenderers: Map<String, InputRenderer>,
    outputRenderers: Map<String, OutputRenderer>,
    private val actions: Actions
) : ViewModel() {
    //region TODO: Internals to be refactored to somewhere

    class Renderers(
        val input: Map<String, (LeancherIntent.Value.Reference, (() -> Unit)?) -> (@Composable () -> Unit)>,
        val output: Map<String, @Composable () -> Unit>
    )

    private val renderers = Renderers(
        inputRenderers.mapValues { (_, renderFunction) ->
            { reference, action ->
                {
                    renderFunction { value ->
                        values[reference.key] = value
                        action?.invoke()
                    }
                }
            }
        },
        outputRenderers
    )

    @Composable
    fun NoRendererDefined() = Text("No Renderer defined")
    //endregion

    private val values: MutableMap<String, Any> = mutableMapOf()

    private val intents = leancher.android.domain.intents.intents

    var blocks: List<Block> by mutableStateOf(listOf())
        private set
    var currentBlock: CurrentBlock? by mutableStateOf(null)
        private set
    private val steps get() = blocks.map { block -> block.step }
    private val stepIndex get() = steps.size
    private val stepIds get() = steps.map { step -> step.id }
    private val matchingIntents get() = intents.filter { intent -> intent.matches(stepIds) }
    private val nextSteps get() = matchingIntents.mapNotNull { intent -> intent.blocks.getOrNull(stepIndex) }

    private fun addBlock(block: Block) { blocks = blocks + block }
    private fun addBlock(step: LeancherIntent.Step.Text) = addBlock(Block.Text(step))
    private fun addBlock(step: LeancherIntent.Step.Getter) = addBlock(Block.Result(
        step,
        renderers.output[step.resultRenderer?.id] ?: { NoRendererDefined() }
    ))
    private fun addBlock(step: LeancherIntent.Step.Action) = addBlock(Block.Action(step))
    private fun addBlock(step: LeancherIntent.Step.Message) = addBlock(Block.Message(step))

    fun start() = advance()

    private fun startOver() {
        blocks = listOf()
        clearCurrentBlock()
    }

    private fun clearCurrentBlock() {
        currentBlock = null
    }

    private fun doStep(step: LeancherIntent.Step) {
        when(step) {
            is LeancherIntent.Step.Getter.InputGetter -> currentBlock = CurrentBlock.Input(
                renderers.input[step.inputRenderer.id]?.invoke(step.reference) {
                    clearCurrentBlock()
                    addBlock(step)
                    advance()
                } ?: { NoRendererDefined() })
            is LeancherIntent.Step.Getter.IntentGetter -> {
                TODO()
            }
            is LeancherIntent.Step.Action.LaunchIntentByReference -> {
                actions.executeIntent(values[step.reference.key] as Intent)
                addBlock(step)
                advance()
            }
            is LeancherIntent.Step.Action.LaunchIntentByDefinition -> {
                actions.executeIntent(createIntent(step.definition))
                addBlock(step)
                advance()
            }
            is LeancherIntent.Step.Message -> {
                addBlock(step)
                advance()
            }
            else -> throw Exception("maybe try exhaustive switches ;)")
        }
    }
    private fun createSelection() {
        currentBlock = CurrentBlock.Selection(
            intents
                .filter { intent -> intent.matches(stepIds) }
                .mapNotNull { intent -> intent.blocks.elementAtOrNull(stepIndex) }
                .filterIsInstance<LeancherIntent.Step.Text>()
                .map { step -> SelectionOption(step.content) {
                    clearCurrentBlock()
                    addBlock(step)
                    advance()
                }})
    }

    private fun advance() {
        // region behavioral bindings
        fun isIntentFinished() = matchingIntents.size == 1 && nextSteps.isEmpty()
        fun isSelectionNeeded() = nextSteps.size > 1
        // endregion
        when {
            isIntentFinished() -> startOver()
            isSelectionNeeded() -> createSelection()
            else -> doStep(nextSteps.first())
        }
    }


    private fun <T> resolveValue(registry: Map<String, Any>, value: LeancherIntent.Value): T? =
        when (value) {
            is LeancherIntent.Value.Reference -> registry[value.key]
            is LeancherIntent.Value.Constant -> value.value
        } as T?

    private fun createIntent(definition: LeancherIntent.IntentDefinition): Intent =
        Intent(definition.id).apply {
            when (definition.additional) {
                is LeancherIntent.IntentDefinition.Additional.Data -> setDataAndNormalize(
                    Uri.parse(
                        resolveValue(
                            values,
                            definition.additional.content
                        )
                    )
                )
                is LeancherIntent.IntentDefinition.Additional.Type -> setTypeAndNormalize(resolveValue(values, definition.additional.content))
            }

            definition.extras?.forEach { definition ->
                putExtra(
                    definition.id,
                    resolveValue<Serializable>(values, definition.value)
                )
            }
        }

    data class Actions(
        val executeIntent: (Intent) -> Unit,
        val isIntentCallable: (Intent) -> Boolean
    )
}