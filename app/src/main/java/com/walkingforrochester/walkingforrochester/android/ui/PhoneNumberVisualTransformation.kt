package com.walkingforrochester.walkingforrochester.android.ui

import android.content.Context
import android.content.pm.ApplicationInfo
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.text.Selection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.core.content.getSystemService
import com.google.i18n.phonenumbers.AsYouTypeFormatter
import com.google.i18n.phonenumbers.PhoneNumberUtil
import timber.log.Timber
import java.util.Locale

class PhoneNumberVisualTransformation(
    context: Context
) : VisualTransformation {
    private val phoneNumberFormatter: AsYouTypeFormatter
    private val isDebuggable: Boolean

    init {
        // Using try catch because preview throws exception trying to get telephony manager
        val telephonyManager: TelephonyManager? = try {
            context.applicationContext.getSystemService()
        } catch (t: Throwable) {
            Timber.w("Unable to get telephony manager: %s", t.message)
            null
        }
        val countryCode = determineCountryCode(telephonyManager)
        phoneNumberFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(countryCode)

        isDebuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    private fun determineCountryCode(telephonyManager: TelephonyManager?): String {
        val simCountryIso = telephonyManager?.simCountryIso?.uppercase()
        val networkCountryIso = telephonyManager?.networkCountryIso?.uppercase()
        return when {
            !simCountryIso.isNullOrBlank() -> simCountryIso
            !networkCountryIso.isNullOrBlank() -> networkCountryIso
            else -> Locale.getDefault().country
        }
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val transformation = reformat(text, Selection.getSelectionEnd(text))

        return TransformedText(
            AnnotatedString(transformation.formatted ?: ""),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return transformation.originalToTransformed[offset]
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return transformation.transformedToOriginal[offset]
                }
            })
    }

    private fun reformat(s: CharSequence, cursor: Int): Transformation {
        phoneNumberFormatter.clear()

        val curIndex = cursor - 1
        var formatted: String? = null
        var lastNonSeparator = 0.toChar()
        var hasCursor = false

        s.forEachIndexed { index, char ->
            if (PhoneNumberUtils.isNonSeparator(char)) {
                if (lastNonSeparator.code != 0) {
                    formatted = getFormattedNumber(lastNonSeparator, hasCursor)
                    hasCursor = false
                }
                lastNonSeparator = char
            }
            if (index == curIndex) {
                hasCursor = true
            }
        }

        if (lastNonSeparator.code != 0) {
            formatted = getFormattedNumber(lastNonSeparator, hasCursor)
        }
        val originalToTransformed = mutableListOf<Int>()
        val transformedToOriginal = mutableListOf<Int>()

        var originalOffset = 0
        val lastIndex = s.lastIndex

        // Map the position of the character in new string to original
        formatted?.forEachIndexed { index, char ->

            transformedToOriginal.add(originalOffset.coerceAtMost(lastIndex))

            if (originalOffset <= lastIndex && char == s[originalOffset]) {
                originalToTransformed.add(index)
                ++originalOffset
            }
        }
        if (originalToTransformed.size != s.length) {
            Timber.w("Original is longer than transformed. Make sure illegal characters are being filtered when updating view model")
            if (!isDebuggable) {
                // Safety to prevent crash. Will crash in debug mode
                val pos = originalToTransformed.size
                for (i in pos until s.length) {
                    originalToTransformed.add(formatted?.lastIndex ?: 0)
                }
            }
        }

        val formattedText = formatted
        originalToTransformed.add(if (formattedText.isNullOrEmpty()) 0 else formattedText.lastIndex.plus(1))
        transformedToOriginal.add(if (s.isNotEmpty()) s.lastIndex.plus(1) else 0)
        //Timber.d("original: %s", originalToTransformed.toString())
        //Timber.d("transform: %s", transformedToOriginal.toString())

        return Transformation(formatted, originalToTransformed, transformedToOriginal)
    }

    private fun getFormattedNumber(lastNonSeparator: Char, hasCursor: Boolean): String? {
        return if (hasCursor) {
            phoneNumberFormatter.inputDigitAndRememberPosition(lastNonSeparator)
        } else {
            phoneNumberFormatter.inputDigit(lastNonSeparator)
        }
    }

    private data class Transformation(
        val formatted: String?,
        val originalToTransformed: List<Int>,
        val transformedToOriginal: List<Int>
    )
}