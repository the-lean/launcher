package leancher.android.domain.intents

import leancher.android.domain.intents.LeancherIntent.Block.Action.*
import leancher.android.domain.intents.LeancherIntent.Block.Action.Getter.InputGetter
import leancher.android.domain.intents.LeancherIntent.Block.Action.IntentDefinition.Extra
import leancher.android.domain.intents.LeancherIntent.Block.Action.IntentDefinition.Value.Constant
import leancher.android.domain.intents.LeancherIntent.Block.Action.IntentDefinition.Value.Reference
import leancher.android.domain.intents.LeancherIntent.Block.Action.Setter.IntentDefinitionSetter
import leancher.android.domain.intents.LeancherIntent.Block.Action.Setter.ReferenceSetter
import leancher.android.domain.intents.LeancherIntent.Block.Message
import leancher.android.domain.intents.LeancherIntent.Block.Text

val app = LeancherIntent(
    Text("start an app"),
    InputGetter(Reference("launchIntent"), InputRenderer("AppList")),
//    ReferenceSetter(Reference("launchIntent"))
    Message("i will be soon ready to launch something for you")
)

val alarm = LeancherIntent(
    Text("set an alarm"),
    IntentDefinitionSetter(IntentDefinition("android.intent.action.SET_ALARM"))
)

val timer = LeancherIntent(
    Text("set a timer"),
    IntentDefinitionSetter(IntentDefinition("android.intent.action.SET_TIMER")))

val nap = LeancherIntent(
    Text("nap"),
    IntentDefinitionSetter(
        IntentDefinition(
            "android.intent.action.SET_TIMER",
            extras = listOf(
                Extra("android.intent.extra.alarm.LENGTH", Constant(720)),
                Extra("android.intent.extra.alarm.MESSAGE", Constant("💤")),
                Extra("android.intent.extra.alarm.SKIP_UI", Constant(true))
            )
        )
    ),
    Message("sleep well...")
)

val intents = listOf(app, alarm, timer, nap)