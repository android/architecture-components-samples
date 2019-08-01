package com.example.android.navigationadvancedsample

import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

object FormatUtil {
    private fun f1(pattern: String) =
        pattern to DecimalFormat(pattern).apply { roundingMode = RoundingMode.DOWN }

    private fun f2(pattern: String, locale: Locale) =
        (pattern to locale) to SimpleDateFormat(pattern, locale)

    private val DECIMAL_FORMATS = hashMapOf(
        f1(",##0"),
        f1(",##0.0"),
        f1(",##0.00"),
        f1(",##0.000"),
        f1(",##0.0#"),
        f1(",##0.0###"),
        f1(",##0.0#####"),
        f1("#0"),
        f1("#0.0"),
        f1("#0.00"),
        f1("#0.000"),
        f1("#0.0#"),
        f1("#0.0###"),
        f1("#0.0#####")
    )

    /**
     * 韩、日语都默认使用 Locale.US
     */
    private val DATE_FORMATS = hashMapOf(
        // 常用
        f2("yyyy-MM-dd HH:mm:ss", Locale.US),
        f2("yyyy-MM-dd HH:mm:ss", Locale.CHINA),
        f2("HH:mm:ss", Locale.US),
        f2("HH:mm:ss", Locale.CHINA),
        // 首页分时线相关
        f2("HH:mm a", Locale.US),
        f2("a HH:mm", Locale.CHINA),
        f2("E, MMM dd, yyyy", Locale.US),
        f2("yyyy 年 MM 月 dd 日 E", Locale.CHINA),
        f2("HH:mm", Locale.US),
        f2("HH:mm", Locale.CHINA),
        f2("MM/dd", Locale.US),
        f2("MM/dd", Locale.CHINA)

    )

    fun formatNumber(number: Number, precision: Int? = null, thousandSeparator: Boolean = true): String {
        return try {
            val base = if (thousandSeparator) {
                ",##0"
            } else {
                "#0"
            }
            val key = when {
                precision == null -> {
                    when {
                        abs(number.toFloat()) > 0.01 -> "$base.0#"
                        abs(number.toFloat()) > 0.0001 -> "$base.0###"
                        else -> "$base.0#####"
                    }
                }
                precision <= 0 -> base
                else -> "$base.${"0".repeat(precision)}"
            }

            val f = if (key in DECIMAL_FORMATS) {
                DECIMAL_FORMATS[key]!!
            } else {
                val newF = DecimalFormat(key)
                DECIMAL_FORMATS[key] = newF
                newF
            }

            return f.format(number)
        } catch (e: Exception) {
            ""
        }
    }

    fun formatDate(pattern: String, ts: Long, locale: Locale = Locale.getDefault()): String {
        val locale = if (locale == Locale.CHINA) {
            locale
        } else {
            Locale.US
        }

        return try {
            val key = pattern to locale

            val f = if (key in DATE_FORMATS) {
                DATE_FORMATS[key]!!
            } else {
                val newF = SimpleDateFormat(pattern, locale)
                DATE_FORMATS[key] = newF
                newF
            }

            return f.format(Date(ts))
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 首页分时图的时间
     */
    fun timeLineDates(ts: Long, locale: Locale = Locale.getDefault()): Pair<String, String> {
        val locale = if (locale == Locale.CHINA) {
            locale
        } else {
            Locale.US
        }

        val key = when (locale) {
            Locale.CHINA -> "a HH:mm" to locale
            else -> "HH:mm a" to locale
        }
        val time = DATE_FORMATS[key]!!.format(ts)
        val key2 = when (locale) {
            Locale.CHINA -> "yyyy 年 MM 月 dd 日 E" to locale
            else -> "E, MMM dd, yyyy" to locale
        }
        val date = DATE_FORMATS[key2]!!.format(ts)

        return time to date
    }

    /**
     * 首页分时图底部 x 轴的 label
     */
    fun getTimeLineLabelFormats(locale: Locale = Locale.getDefault()): Pair<DateFormat, DateFormat> {
        val locale = if (locale == Locale.CHINA) {
            locale
        } else {
            Locale.US
        }

        val key = "HH:mm" to locale
        val key2 = "MM/dd" to locale
        return DATE_FORMATS[key]!! to DATE_FORMATS[key2]!!
    }
}