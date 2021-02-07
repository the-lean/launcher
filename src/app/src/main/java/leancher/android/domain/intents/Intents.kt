package leancher.android.domain.intents

import leancher.android.domain.intents.LeancherIntent.IntentDefinition
import leancher.android.domain.intents.LeancherIntent.IntentDefinition.Extra
import leancher.android.domain.intents.LeancherIntent.Step.Action.LaunchIntentByDefinition
import leancher.android.domain.intents.LeancherIntent.Step.Action.LaunchIntentByReference
import leancher.android.domain.intents.LeancherIntent.Step.Getter.InputGetter
import leancher.android.domain.intents.LeancherIntent.Step.Getter.IntentGetter
import leancher.android.domain.intents.LeancherIntent.Step.Message
import leancher.android.domain.intents.LeancherIntent.Step.Text
import leancher.android.domain.intents.LeancherIntent.Value

val app = LeancherIntent(
    Text("start an app"),
    InputGetter(Value.Reference("app"), InputRenderer("AppList")),
    LaunchIntentByReference(Value.Reference("app"))
)

val call = LeancherIntent(
    Text("call"),
    IntentGetter(
        IntentDefinition(
            "android.intent.action.PICK",
            IntentDefinition.Additional.Type(Value.Constant("TODO: value of ContactsContract.Contacts.CONTENT_URI"))
        ),
        Value.Reference("contact")
    ),
    LaunchIntentByDefinition(IntentDefinition("TODO: Call somebody"))
)

val alarm = LeancherIntent(
    Text("set an alarm"),
    LaunchIntentByDefinition(IntentDefinition("android.intent.action.SET_ALARM"))
)

val timer = LeancherIntent(
    Text("set a timer"),
    LaunchIntentByDefinition(IntentDefinition("android.intent.action.SET_TIMER")))

val nap = LeancherIntent(
    Text("nap"),
    LaunchIntentByDefinition(
        IntentDefinition(
            "android.intent.action.SET_TIMER",
            extras = listOf(
                Extra("android.intent.extra.alarm.LENGTH", Value.Constant(720)),
                Extra("android.intent.extra.alarm.MESSAGE", Value.Constant("💤")),
                Extra("android.intent.extra.alarm.SKIP_UI", Value.Constant(true))
            )
        )
    ),
    Message("sleep well...")
)

val intents = listOf(app, call, alarm, timer, nap)