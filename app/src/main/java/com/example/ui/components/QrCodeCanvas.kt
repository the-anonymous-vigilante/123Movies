package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

enum class CenterLogoType {
    NONE,
    REDOT_PAY,
    NSAVE,
    APP_LOGO
}

@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    codeColor: Color = Color.Black,
    centerLogo: CenterLogoType = CenterLogoType.NONE,
    contentPadding: Dp = 12.dp
) {
    // Generate real, standard ZXing QR bitmap
    val qrBitmap = remember(data, centerLogo) {
        generateQrBitmap(
            content = data,
            size = 512,
            centerLogo = centerLogo
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        if (qrBitmap != null) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "Scannable QR Code",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Encodes a text/URL string into a standard, 100% scannable QR code Bitmap.
 * Uses high error correction (Level H) so center logos or badges can be embedded
 * without affecting scanner readability.
 */
fun generateQrBitmap(
    content: String,
    size: Int = 512,
    codeColor: Int = android.graphics.Color.BLACK,
    backgroundColor: Int = android.graphics.Color.WHITE,
    centerLogo: CenterLogoType = CenterLogoType.NONE
): Bitmap? {
    if (content.isBlank()) return null
    return try {
        val hints = hashMapOf<EncodeHintType, Any>(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 1,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H
        )

        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) codeColor else backgroundColor)
            }
        }

        // Draw center logo if requested
        if (centerLogo != CenterLogoType.NONE) {
            val canvas = Canvas(bitmap)
            val centerX = width / 2f
            val centerY = height / 2f
            val logoRadius = width * 0.115f

            // White border ring around center logo to preserve QR alignment
            val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawCircle(centerX, centerY, logoRadius + 8f, ringPaint)

            when (centerLogo) {
                CenterLogoType.REDOT_PAY -> {
                    // Red circle with white 'P' matching RedotPay branding
                    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.parseColor("#E50914")
                        style = Paint.Style.FILL
                    }
                    canvas.drawCircle(centerX, centerY, logoRadius, circlePaint)

                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.WHITE
                        textSize = logoRadius * 1.35f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }
                    val textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2f)
                    canvas.drawText("P", centerX, textY, textPaint)
                }
                CenterLogoType.NSAVE -> {
                    // Modern Nsave badge
                    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.parseColor("#111318")
                        style = Paint.Style.FILL
                    }
                    canvas.drawCircle(centerX, centerY, logoRadius, circlePaint)

                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.parseColor("#E50914")
                        textSize = logoRadius * 1.1f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }
                    val textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2f)
                    canvas.drawText("N", centerX, textY, textPaint)
                }
                CenterLogoType.APP_LOGO -> {
                    // Cinema Red '123' badge
                    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.parseColor("#E50914")
                        style = Paint.Style.FILL
                    }
                    canvas.drawCircle(centerX, centerY, logoRadius, circlePaint)

                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.WHITE
                        textSize = logoRadius * 0.9f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }
                    val textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2f)
                    canvas.drawText("123", centerX, textY, textPaint)
                }
                CenterLogoType.NONE -> Unit
            }
        }

        bitmap
    } catch (e: Exception) {
        null
    }
}
