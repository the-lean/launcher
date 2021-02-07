package leancher.android.domain.intents

sealed class StepId {
    data class TextId(val text: String) : StepId()
    data class GetterId(val id: String, val reference: String) : StepId()
    data class SetterId(val id: String) : StepId()
    data class MessageId(val message: String) : StepId()
}

data class InputRenderer(val id: String)
data class ResultRenderer(val id: String)

class LeancherIntent(val blocks: List<Step>) {
    constructor(vararg blocks: Step) : this(blocks.asList())

    fun matches(ids: List<StepId>) : Boolean =
        (ids zip blocks.map { step -> step.id })
            .fold(true) { acc, pair ->
                acc && pair.run { first == second }
            }

    sealed class Step {
        abstract val id: StepId

        override fun equals(other: Any?): Boolean = other is Step && this.id == other.id

        data class Text(val content: String) : Step() {
            override val id = StepId.TextId(content)
        }

        sealed class Getter(open val resultRenderer: ResultRenderer? = null) : Step() {
            data class InputGetter(
                val reference: Value.Reference,
                val inputRenderer: InputRenderer,
                override val resultRenderer: ResultRenderer? = null
            ) : Getter(resultRenderer) {
                override val id = StepId.GetterId(inputRenderer.id, reference.key)
            }
            data class IntentGetter(
                val definition: IntentDefinition,
                val reference: Value.Reference,
                override val resultRenderer: ResultRenderer? = null
            ) : Getter() {
                override val id = StepId.GetterId(definition.id, reference.key)
            }
        }

        sealed class Action : Step() {
            data class LaunchIntentByReference(val reference: Value.Reference) : Action() {
                override val id = StepId.SetterId("ref:${reference.key}")
            }
            data class LaunchIntentByDefinition(val definition: IntentDefinition) : Action() {
                override val id = StepId.SetterId("intent:${definition.id}")
            }
        }

        data class Message(val content: String) : Step() {
            override val id = StepId.MessageId(content)
        }
    }

    sealed class Value {
        data class Reference(val key: String) : Value()
        data class Constant(val value: Any) : Value()
    }

    data class IntentDefinition(
        val id: String,
        val additional: Additional? = null,
        val extras: List<Extra>? = null) {
        data class Extra(val id: String, val value: Value)

        sealed class ChooserDefinition {
            object NoChooser : ChooserDefinition()
            data class Chooser(val title: String) : ChooserDefinition()
        }

        sealed class Additional {
            data class Data(val content: Value) : Additional()
            data class Type(val content: Value) : Additional()
        }
    }
}