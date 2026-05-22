package com.pointcheck.core.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class RutVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val cleanText = text.text.replace(".", "").replace("-", "")
        if (cleanText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val out = StringBuilder()
        val dv = if (cleanText.length > 1) cleanText.takeLast(1) else ""
        val body = if (cleanText.length > 1) cleanText.dropLast(1) else cleanText

        if (body.isNotEmpty()) {
            val reversedBody = body.reversed()
            for (i in reversedBody.indices) {
                out.append(reversedBody[i])
                if ((i + 1) % 3 == 0 && i != reversedBody.length - 1) {
                    out.append(".")
                }
            }
            out.reverse()
        }

        if (dv.isNotEmpty()) {
            out.append("-")
            out.append(dv)
        }

        val rutOffsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val transformedStr = out.toString()
                var originalCount = 0
                for (i in transformedStr.indices) {
                    if (transformedStr[i] != '.' && transformedStr[i] != '-') {
                        originalCount++
                    }
                    if (originalCount == offset) {
                        return i + 1
                    }
                }
                return transformedStr.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val transformedStr = out.toString()
                var originalCount = 0
                val limitedOffset = offset.coerceAtMost(transformedStr.length)
                for (i in 0 until limitedOffset) {
                    if (transformedStr[i] != '.' && transformedStr[i] != '-') {
                        originalCount++
                    }
                }
                return originalCount
            }
        }

        return TransformedText(AnnotatedString(out.toString()), rutOffsetMapping)
    }
}
