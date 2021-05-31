package com.example.zrenie20.base.adapters

open class TimeDelegate(var time: String): StickDelegate(time)

open class StatisticsHeaderDataDelegate(
        val leftName: String,
        val rightName: String,
        stickId: String
): StickDelegate(stickId)

open class StickDelegate(var stickId: String)