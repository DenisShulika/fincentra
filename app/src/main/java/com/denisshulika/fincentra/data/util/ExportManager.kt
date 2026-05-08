package com.denisshulika.fincentra.data.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.denisshulika.fincentra.R
import com.denisshulika.fincentra.data.models.domain.Transaction
import com.denisshulika.fincentra.data.network.common.CurrencyMapper
import java.io.File
import java.io.FileOutputStream

object ExportManager {

    fun generatePdf(
        context: Context,
        transactions: List<Transaction>,
        includeHeader: Boolean,
        includeSummary: Boolean
    ): Uri? {
        if (transactions.isEmpty()) return null

        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        var currentPageNumber = 1

        var pageInfo =
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint().apply { isAntiAlias = true }

        try {
            var y = 40f

            if (includeHeader) {
                paint.color = Color.parseColor("#16A34A")
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, paint)

                paint.color = Color.WHITE
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 24f
                canvas.drawText(context.getString(R.string.app_name), 25f, 45f, paint)

                paint.textSize = 12f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(
                    context.getString(R.string.export_manager_pdf_statement),
                    25f,
                    65f,
                    paint
                )
                canvas.drawText(
                    context.getString(
                        R.string.export_manager_pdf_generated,
                        DateFormatter.formatFullDate(System.currentTimeMillis())
                    ),
                    25f, 80f, paint
                )
                y = 120f
            }

            if (includeSummary) {
                paint.color = Color.BLACK
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 14f
                canvas.drawText(
                    context.getString(R.string.export_manager_pdf_summary),
                    25f,
                    y,
                    paint
                )
                y += 22f

                val totalInc = transactions.filter { !it.isExpense }.sumOf { it.amount }
                val totalExp = transactions.filter { it.isExpense }.sumOf { it.amount }

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 12f
                canvas.drawText(
                    context.getString(
                        R.string.export_manager_pdf_total_income,
                        String.format("%.2f", totalInc)
                    ), 25f, y, paint
                )
                y += 18f

                canvas.drawText(
                    context.getString(
                        R.string.export_manager_pdf_total_expenses,
                        String.format("%.2f", totalExp)
                    ), 25f, y, paint
                )
                y += 40f
            }

            drawTableHeaders(context, canvas, paint, y)
            y += 30f

            transactions.forEachIndexed { index, tx ->
                if (y > 800f) {
                    pdfDocument.finishPage(page)
                    currentPageNumber++
                    pageInfo =
                        PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber)
                            .create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 40f
                    drawTableHeaders(context, canvas, paint, y)
                    y += 30f
                }

                drawTransactionRow(canvas, paint, tx, y, index)
                y += 22f
            }

            pdfDocument.finishPage(page)

            val reportDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val file = File(reportDir, "FinCentra_Report.pdf")
            FileOutputStream(file).use { pdfDocument.writeTo(it) }
            pdfDocument.close()

            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            pdfDocument.close()
            return null
        }
    }

    private fun drawTableHeaders(context: Context, canvas: Canvas, paint: Paint, y: Float) {
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(20f, y - 15f, 575f, y + 10f, paint)
        paint.color = Color.BLACK
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(context.getString(R.string.export_manager_table_date), 25f, y, paint)
        canvas.drawText(
            context.getString(R.string.export_manager_table_description),
            100f,
            y,
            paint
        )
        canvas.drawText(context.getString(R.string.export_manager_table_category), 340f, y, paint)
        canvas.drawText(context.getString(R.string.export_manager_table_amount), 480f, y, paint)
    }

    private fun drawTransactionRow(
        canvas: Canvas,
        paint: Paint,
        tx: Transaction,
        y: Float,
        index: Int
    ) {
        if (index % 2 == 0) {
            paint.color = Color.parseColor("#F8FAFC")
            canvas.drawRect(20f, y - 15f, 575f, y + 7f, paint)
        }
        paint.color = Color.BLACK
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        val date = DateFormatter.formatFullDate(tx.timestamp)
        val symbol = CurrencyMapper.getSymbol(tx.currencyCode)
        val amountText =
            "${if (tx.isExpense) "-" else "+"}${String.format("%.2f", tx.amount)} $symbol"

        canvas.drawText(date, 25f, y, paint)
        canvas.drawText(tx.description.take(35), 100f, y, paint)
        canvas.drawText(
            tx.category.name.lowercase().replaceFirstChar { it.uppercase() },
            340f,
            y,
            paint
        )

        paint.color = if (tx.isExpense) Color.parseColor("#EF4444") else Color.parseColor("#16A34A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(amountText, 480f, y, paint)
    }

    fun generateCsv(
        context: Context,
        transactions: List<Transaction>,
        includeSummary: Boolean
    ): Uri? {
        if (transactions.isEmpty()) return null

        val sep = "\t"
        val csvContent = StringBuilder()

        if (includeSummary) {
            val totalInc = transactions.filter { !it.isExpense }.sumOf { it.amount }
            val totalExp = transactions.filter { it.isExpense }.sumOf { it.amount }
            csvContent.append("${context.getString(R.string.export_manager_csv_summary_header)}\n")
            csvContent.append(
                "${context.getString(R.string.export_manager_csv_label_income)}${sep}+${
                    String.format(
                        "%.2f",
                        totalInc
                    )
                }\n"
            )
            csvContent.append(
                "${context.getString(R.string.export_manager_csv_label_expenses)}${sep}-${
                    String.format(
                        "%.2f",
                        totalExp
                    )
                }\n\n"
            )
        }

        csvContent.append(
            "${context.getString(R.string.export_manager_table_date)}${sep}${
                context.getString(
                    R.string.export_manager_table_description
                )
            }${sep}${context.getString(R.string.export_manager_table_category)}${sep}${
                context.getString(
                    R.string.export_manager_table_amount
                )
            }${sep}${context.getString(R.string.export_manager_csv_col_currency)}${sep}${
                context.getString(
                    R.string.export_manager_csv_col_type
                )
            }\n"
        )

        transactions.forEach { tx ->
            val date = DateFormatter.formatFullDate(tx.timestamp)
            val desc = tx.description.replace("\t", " ").trim()
            val cat = tx.category.name.lowercase().replaceFirstChar { it.uppercase() }
            val amount = String.format("%.2f", tx.amount)
            val currency = CurrencyMapper.getCodeName(tx.currencyCode)
            val type =
                if (tx.isExpense) context.getString(R.string.export_manager_type_expense) else context.getString(
                    R.string.export_manager_type_income
                )

            csvContent.append("$date${sep}$desc${sep}$cat${sep}$amount${sep}$currency${sep}$type\n")
        }

        try {
            val reportDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            val file = File(reportDir, "FinCentra_Data.csv")

            val outputStream = FileOutputStream(file)

            outputStream.write(0xFF)
            outputStream.write(0xFE)

            outputStream.write(csvContent.toString().toByteArray(Charsets.UTF_16LE))
            outputStream.close()

            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            return null
        }
    }
}