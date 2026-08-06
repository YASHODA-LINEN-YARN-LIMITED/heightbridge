package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.LorryWeighment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    fun generateCsvReport(context: Context, lorries: List<LorryWeighment>): File? {
        try {
            val fileName = "BallyJute_LorryLedger_${System.currentTimeMillis()}.csv"
            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            val file = File(downloadsDir, fileName)
            val writer = FileOutputStream(file).bufferedWriter()

            // Header
            writer.write("Gate Pass,Vehicle No,Type,Party,Description,Mokam,Marka,Gate Gross (kg),Mill Gross (kg),Elec Gross (kg),Mill Tare (kg),Elec Tare (kg),Net Wt (kg),Status,In Time,Out Time,Date\n")

            lorries.forEach { lorry ->
                val gateGross = lorry.grossWeight ?: 0.0
                val millGross = lorry.millGrossWeight ?: 0.0
                val elecGross = lorry.electricGrossWeight ?: 0.0
                val millTare = lorry.millTareWeight ?: lorry.tareWeight ?: 0.0
                val elecTare = lorry.electricTareWeight ?: 0.0
                val net = lorry.lowestNetWeight ?: ((millGross.takeIf { it > 0 } ?: elecGross.takeIf { it > 0 } ?: gateGross) - (millTare.takeIf { it > 0 } ?: elecTare)).coerceAtLeast(0.0)
                val line = listOf(
                    lorry.gatePass,
                    lorry.lorryNumber,
                    lorry.type,
                    lorry.party,
                    lorry.description,
                    lorry.mokam,
                    lorry.marka,
                    gateGross.toInt().toString(),
                    millGross.toInt().toString(),
                    elecGross.toInt().toString(),
                    millTare.toInt().toString(),
                    elecTare.toInt().toString(),
                    net.toInt().toString(),
                    lorry.status,
                    lorry.inTime,
                    lorry.outTime ?: "",
                    lorry.date
                ).joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }
                writer.write(line + "\n")
            }

            writer.flush()
            writer.close()
            shareOrOpenFile(context, file, "text/csv", "Exported CSV Lorry Ledger")
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error generating CSV: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            return null
        }
    }

    fun generatePdfShiftLedger(context: Context, lorries: List<LorryWeighment>): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subtitlePaint = Paint().apply {
                color = Color.rgb(37, 99, 235)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val textPaint = Paint().apply {
                color = Color.rgb(51, 65, 85)
                textSize = 10f
            }
            val headerBgPaint = Paint().apply {
                color = Color.rgb(241, 245, 249)
            }
            val tableHeaderPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val linePaint = Paint().apply {
                color = Color.rgb(226, 232, 240)
                strokeWidth = 1f
            }

            var y = 40f

            // Banner Header
            canvas.drawText("BALLY JUTE COMPANY LIMITED", 40f, y, titlePaint)
            y += 18f
            canvas.drawText("DAILY SHIFT WEIGHMENT LEDGER STATEMENT", 40f, y, subtitlePaint)
            y += 14f

            val dateStr = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Generated On: $dateStr  |  Total Lorry Records: ${lorries.size}", 40f, y, textPaint)
            y += 15f

            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 15f

            // Summary Table Box
            val totalNetKg = lorries.sumOf { lorry ->
                lorry.lowestNetWeight ?: ((lorry.millGrossWeight ?: lorry.electricGrossWeight ?: lorry.grossWeight ?: 0.0) - (lorry.millTareWeight ?: lorry.electricTareWeight ?: lorry.tareWeight ?: 0.0)).coerceAtLeast(0.0)
            }
            val totalNetMt = Math.round((totalNetKg / 1000.0) * 10.0) / 10.0

            canvas.drawRect(40f, y, 555f, y + 32f, headerBgPaint)
            canvas.drawText("Total Lorries: ${lorries.size}  |  Completed: ${lorries.count { it.status == "COMPLETED" }}  |  Total Net Weight: $totalNetMt MT ($totalNetKg kg)", 48f, y + 20f, tableHeaderPaint)
            y += 42f

            // Table Column Headers
            canvas.drawRect(40f, y, 555f, y + 20f, headerBgPaint)
            canvas.drawText("Gate Pass", 45f, y + 14f, tableHeaderPaint)
            canvas.drawText("Vehicle No", 115f, y + 14f, tableHeaderPaint)
            canvas.drawText("Party Name", 195f, y + 14f, tableHeaderPaint)
            canvas.drawText("Gross (kg)", 335f, y + 14f, tableHeaderPaint)
            canvas.drawText("Tare (kg)", 415f, y + 14f, tableHeaderPaint)
            canvas.drawText("Net (kg)", 485f, y + 14f, tableHeaderPaint)
            y += 20f

            // Data Rows
            lorries.take(30).forEach { lorry ->
                val gross = lorry.millGrossWeight ?: lorry.electricGrossWeight ?: lorry.grossWeight ?: 0.0
                val tare = lorry.millTareWeight ?: lorry.electricTareWeight ?: lorry.tareWeight ?: 0.0
                val net = lorry.lowestNetWeight ?: (gross - tare).coerceAtLeast(0.0)

                canvas.drawText(lorry.gatePass.take(10), 45f, y + 14f, textPaint)
                canvas.drawText(lorry.lorryNumber.take(12), 115f, y + 14f, textPaint)
                canvas.drawText(lorry.party.ifBlank { "N/A" }.take(22), 195f, y + 14f, textPaint)
                canvas.drawText(gross.toInt().toString(), 335f, y + 14f, textPaint)
                canvas.drawText(tare.toInt().toString(), 415f, y + 14f, textPaint)
                canvas.drawText(net.toInt().toString(), 485f, y + 14f, textPaint)

                y += 18f
                canvas.drawLine(40f, y, 555f, y, linePaint)

                if (y > 780f) return@forEach
            }

            pdfDocument.finishPage(page)

            val fileName = "BallyJute_ShiftLedger_${System.currentTimeMillis()}.pdf"
            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            val file = File(downloadsDir, fileName)
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()

            shareOrOpenFile(context, file, "application/pdf", "Bally Jute Shift Ledger PDF")
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error generating PDF Shift Ledger: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            return null
        }
    }

    fun generatePdfWeightReceipt(context: Context, lorry: LorryWeighment): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val subTitlePaint = Paint().apply {
                color = Color.rgb(37, 99, 235)
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val labelPaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val valuePaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val borderPaint = Paint().apply {
                color = Color.rgb(203, 213, 225)
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            val fillBoxPaint = Paint().apply {
                color = Color.rgb(248, 250, 252)
            }

            var y = 60f

            // Outer Slip Frame
            canvas.drawRect(30f, 30f, 565f, 750f, borderPaint)

            // Header Banner
            canvas.drawText("BALLY JUTE COMPANY LIMITED", 297f, y, titlePaint)
            y += 22f
            canvas.drawText("OFFICIAL MILL WEIGHMENT SLIP / RECEIPT", 297f, y, subTitlePaint)
            y += 25f

            canvas.drawLine(45f, y, 550f, y, borderPaint)
            y += 25f

            // Slip Metadata Box
            canvas.drawRect(45f, y, 550f, y + 60f, fillBoxPaint)
            canvas.drawText("GATE PASS NO:", 55f, y + 25f, labelPaint)
            canvas.drawText(lorry.gatePass, 150f, y + 25f, valuePaint)

            canvas.drawText("DATE & TIME:", 330f, y + 25f, labelPaint)
            canvas.drawText("${lorry.date} ${lorry.inTime}", 420f, y + 25f, valuePaint)

            canvas.drawText("VEHICLE NO:", 55f, y + 50f, labelPaint)
            canvas.drawText(lorry.lorryNumber, 150f, y + 50f, valuePaint)

            canvas.drawText("STATUS:", 330f, y + 50f, labelPaint)
            canvas.drawText(lorry.status, 420f, y + 50f, valuePaint)

            y += 80f

            // Material Details
            canvas.drawText("PARTY / SUPPLIER:", 55f, y, labelPaint)
            canvas.drawText(lorry.party.ifBlank { "N/A" }, 180f, y, valuePaint)
            y += 24f

            canvas.drawText("MATERIAL / ITEM:", 55f, y, labelPaint)
            canvas.drawText(lorry.description.ifBlank { "Raw Jute" }, 180f, y, valuePaint)
            y += 24f

            canvas.drawText("MOKAM & MARKA:", 55f, y, labelPaint)
            canvas.drawText("${lorry.mokam.ifBlank { "N/A" }} / ${lorry.marka.ifBlank { "N/A" }}", 180f, y, valuePaint)
            y += 24f

            canvas.drawText("CHALLAN NO:", 55f, y, labelPaint)
            canvas.drawText(lorry.chalan.ifBlank { "N/A" }, 180f, y, valuePaint)
            y += 30f

            canvas.drawLine(45f, y, 550f, y, borderPaint)
            y += 25f

            // Weight Header
            canvas.drawText("WEIGHMENT MEASUREMENTS (KG)", 297f, y, subTitlePaint)
            y += 25f

            val gross = lorry.millGrossWeight ?: lorry.electricGrossWeight ?: lorry.grossWeight ?: 0.0
            val tare = lorry.millTareWeight ?: lorry.electricTareWeight ?: lorry.tareWeight ?: 0.0
            val net = lorry.lowestNetWeight ?: (gross - tare).coerceAtLeast(0.0)

            // Gross Box
            canvas.drawRect(55f, y, 200f, y + 60f, fillBoxPaint)
            canvas.drawText("GROSS WEIGHT", 70f, y + 20f, labelPaint)
            canvas.drawText("${gross.toInt()} kg", 70f, y + 45f, valuePaint)

            // Tare Box
            canvas.drawRect(220f, y, 365f, y + 60f, fillBoxPaint)
            canvas.drawText("TARE WEIGHT", 235f, y + 20f, labelPaint)
            canvas.drawText("${tare.toInt()} kg", 235f, y + 45f, valuePaint)

            // Net Box
            val netHighlightPaint = Paint().apply { color = Color.rgb(220, 252, 231) }
            canvas.drawRect(385f, y, 530f, y + 60f, netHighlightPaint)
            canvas.drawText("NET WEIGHT", 400f, y + 20f, labelPaint)
            val netValPaint = Paint().apply {
                color = Color.rgb(22, 101, 52)
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("${net.toInt()} kg", 400f, y + 45f, netValPaint)

            y += 120f

            // Signatures
            canvas.drawLine(55f, y, 200f, y, borderPaint)
            canvas.drawLine(385f, y, 530f, y, borderPaint)
            y += 18f
            canvas.drawText("Weighbridge Operator", 70f, y, labelPaint)
            canvas.drawText("Authorized Signatory", 400f, y, labelPaint)

            pdfDocument.finishPage(page)

            val fileName = "Receipt_${lorry.gatePass.replace("/", "_")}_${System.currentTimeMillis()}.pdf"
            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            val file = File(downloadsDir, fileName)
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()

            shareOrOpenFile(context, file, "application/pdf", "Bally Jute Weight Slip PDF")
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error generating Weight Receipt PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            return null
        }
    }

    private fun shareOrOpenFile(context: Context, file: File, mimeType: String, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(intent, title).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } else {
                Toast.makeText(context, "$title exported to ${file.name}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "$title exported to ${file.name}", Toast.LENGTH_LONG).show()
        }
    }
}
