package com.example.nexttrain

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File

class TicketsFragment : Fragment(R.layout.fragment_tickets) {
    private lateinit var qrImageView: ImageView
    private val pdfPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { handlePickedPdf(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // np. lista biletów albo coś innego

        val appNameTextView =
            (activity as? MainActivity)?.findViewById<TextView>(R.id.appNameTextView)
        if (appNameTextView != null) {
            appNameTextView.textSize = 25F
            appNameTextView.text = "Active Tickets"
        }

        qrImageView = view.findViewById(R.id.qrImageView)

//        openPdfPicker()

        val pdfButton: MaterialButton = view.findViewById(R.id.pdfButton)
        pdfButton.setOnClickListener {
            openPdfPicker()
        }
    }

    private fun openPdfPicker() {
        val mimeTypes = arrayOf("application/pdf")
        pdfPickerLauncher.launch(mimeTypes)
    }

    private fun handlePickedPdf(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val pdfFile = File(requireContext().cacheDir, "temp_selected.pdf")
            inputStream?.use { input ->
                pdfFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            renderFirstPageFromPdf(pdfFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to open PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderFirstPageFromPdf(pdfFile: File) {
        try {
            PDFBoxResourceLoader.init(requireContext())

            val document = PDDocument.load(pdfFile)
            val pdfStripper = PDFTextStripper()
            val extractedTextFromPdf = pdfStripper.getText(document)

//            val normalny = Regex("""Normalny:\s*(\d+)""").find(extractedTextFromPdf)?.groupValues?.get(1)?.toIntOrNull() ?: 0
//            val ulgowy = Regex("""Ulgowy.*?:\s*(\d+)""").find(extractedTextFromPdf)?.groupValues?.get(1)?.toIntOrNull() ?: 0
//
//            val trasa = Regex("""\* \* ([\p{L} ]+?)\s+([\p{L} ]+?) \* \*""").find(extractedTextFromPdf)?.let {
//                "${it.groupValues[1].trim()}-${it.groupValues[2].trim()}"
//            } ?: "Brak trasy"
//
//            val dataWaznosci = Regex("""Ważny (\d{2}\.\d{2}\.\d{4})""").find(extractedTextFromPdf)?.groupValues?.get(1) ?: ""
//            val godzinaOdjazdu = Regex("""Bilet ważny 3 godzin od (\d{2}:\d{2})""").find(extractedTextFromPdf)?.groupValues?.get(1) ?: ""
//            val pociagNr = Regex("""Train REGIO No\. (\d+)""").find(extractedTextFromPdf)?.groupValues?.get(1) ?: ""
//
//            val wlasciciel = Regex("""Właściciel: (.+)""").find(extractedTextFromPdf)?.groupValues?.get(1) ?: ""
//            val cena = Regex("""Opłata za przejazd: ([\d,\.]+) zł""").find(extractedTextFromPdf)?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

            val ticketText: String = extractedTextFromPdf // już wczytany wcześniej
            val ticket: TicketData = parseTicketText(ticketText)

            document.close()

            Log.e("PDF", "Ticket: $ticket")
            Log.e("PDF", "ExtractedData: $ticketText")
            updateTicketUI(ticket)

            val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)

            if (pdfRenderer.pageCount > 0) {
                val page = pdfRenderer.openPage(0)

                val scale = 3
                val bitmap = Bitmap.createBitmap(page.width * scale, page.height * scale, Bitmap.Config.ARGB_8888)
                val rect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)

                page.render(bitmap, rect, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                detectAndCropQrCode(bitmap)
                qrImageView.setImageBitmap(bitmap)
            }

            pdfRenderer.close()
            fileDescriptor.close()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to render PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun detectAndCropQrCode(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val options = BarcodeScannerOptions.Builder()
            .enableAllPotentialBarcodes()
            .build()

        val scanner = BarcodeScanning.getClient(options)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val barcode = barcodes[0] // pierwszy znaleziony

                    barcode.boundingBox?.let { boundingBox ->
                        try {
                            val qrBitmap = Bitmap.createBitmap(
                                bitmap,
                                boundingBox.left.coerceAtLeast(0),
                                boundingBox.top.coerceAtLeast(0),
                                boundingBox.width().coerceAtMost(bitmap.width - boundingBox.left),
                                boundingBox.height().coerceAtMost(bitmap.height - boundingBox.top)
                            )

                            qrImageView.setImageBitmap(qrBitmap)

//                            Log.e("VALUE TYPE", "Code detected: ${barcode.valueType}")
//                            Log.e("FORMAT TYPE", "Code detected: ${barcode.format}")

                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(requireContext(), "Failed to crop code", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "No code found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to detect code", Toast.LENGTH_SHORT).show()
            }
    }

    private fun parseTicketText(text: String): TicketData {
        val normalTicket = Regex("""Normalny:\s*(\d+)""").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val reducedTicket = Regex("""Ulgowy.*?:\s*(\d+)""").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        val owner = Regex("""Właściciel: (.+)""").find(text)?.groupValues?.get(1) ?: ""
        val priceRegex = Regex("""Opłata za\s*\n?\s*przejazd:\s*([\d,.]+)\s*[\u00A0 ]?zł""")
        val price = priceRegex.find(text)?.groupValues?.get(1)
            ?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

        val ticketNumber = Regex("""\b([A-Z]{2}\d{8})\b""").find(text)?.groupValues?.get(1) ?: ""
        val carrierMatch = Regex("""Przewoźnik:\s*(\S+)\s+Rodzaj pociągu:\s*(\S+)""").find(text)
        val carrier = carrierMatch?.groupValues?.get(1) ?: ""
        val trainType = carrierMatch?.groupValues?.get(2) ?: ""

        val discountMatch = Regex("""([A-Z/]+)\s+(\d{1,2})%""").find(text)
        val discountCategory = discountMatch?.groupValues?.get(1) ?: ""
        val discountPercent = discountMatch?.groupValues?.get(2)?.toIntOrNull() ?: 0

        val travelDistance = Regex("""KM:\s*(\d{1,4})""").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        val routes = Regex("""\* \* ([\p{L} ]+?) {2}([\p{L} ]+?) \* \*""").findAll(text).toList()
        val route: String
        var typeForPeriodicIfExist = "ERROR"
        when (routes.size) {
            0 -> route = "Brak"
            1 -> {
                route = "${routes[0].groupValues[1].trim()} → ${routes[0].groupValues[2].trim()}"
                typeForPeriodicIfExist = "MIESIĘCZNY TAM"
            }
            2 -> {
                route = "${routes[0].groupValues[1].trim()} → ${routes[0].groupValues[2].trim()}"
                typeForPeriodicIfExist = "MIESIĘCZNY T/P"
            }
            else -> route = "ERROR"
        }

        // czy okresowy?
        val periodicMatch = Regex("""Ważny od (\d{2}\.\d{2}\.\d{4}) do (\d{2}\.\d{2}\.\d{4})""").find(text)

        return if (periodicMatch != null) {
            TicketData( // okresowy
                type = typeForPeriodicIfExist, //2
                normalTicket = normalTicket, //2
                reducedTicket = reducedTicket, //2
                route = route, //1
                owner = owner, //1
                price = price, //3
                ticketNumber = ticketNumber, //0
                carrier = carrier, //3
                trainType = trainType, //3
                discountCategory = discountCategory, //2 +1
                discountPercent = discountPercent, //2 +2
                travelDistance = travelDistance, //3
                startDate = periodicMatch.groupValues[1], //2
                endDate = periodicMatch.groupValues[2], //2
                validityDate = null,
                departureTime = null,
                trainNumber = null
            )
        } else {
            val validityDate = Regex("""Ważny (\d{2}\.\d{2}\.\d{4})""").find(text)?.groupValues?.get(1)
            val departureTime = Regex("""Bilet ważny 3 godzin od (\d{2}:\d{2})""").find(text)?.groupValues?.get(1)
            val trainNumber = Regex("""Train REGIO No\. (\d+)""").find(text)?.groupValues?.get(1)

            TicketData( // jednorazowy
                type = "PRZEJAZD TAM",
                normalTicket = normalTicket,
                reducedTicket = reducedTicket,
                route = route,
                owner = owner,
                price = price,
                ticketNumber = ticketNumber,
                carrier = carrier,
                trainType = trainType,
                discountCategory = discountCategory,
                discountPercent = discountPercent,
                travelDistance = travelDistance,
                startDate = null,
                endDate = null,
                validityDate = validityDate,
                departureTime = departureTime,
                trainNumber = trainNumber
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTicketUI(ticket: TicketData) {
        if (ticket.type == "PRZEJAZD TAM") {
            //mimo ze takie id to trudno sie mowi
            view?.findViewById<TextView>(R.id.startDate)?.text = "Ważny do: ${ticket.validityDate}" //validity date
            view?.findViewById<TextView>(R.id.endDate)?.text = "Data odjazdu: ${ticket.departureTime}" //departure time
            val trainNumber = view?.findViewById<TextView>(R.id.trainNumber)
            trainNumber?.text = "Numer pocigu: ${ticket.trainNumber}"
            trainNumber?.visibility = VISIBLE
        } else {
            view?.findViewById<TextView>(R.id.startDate)?.text = "Ważny od: ${ticket.startDate}" //validity date
            view?.findViewById<TextView>(R.id.endDate)?.text = "Ważny do: ${ticket.endDate}" //departure time
            view?.findViewById<TextView>(R.id.trainNumber)?.visibility = GONE
        }
        view?.findViewById<TextView>(R.id.ticketNumber)?.text = ticket.ticketNumber
        view?.findViewById<TextView>(R.id.route)?.text = ticket.route
        view?.findViewById<TextView>(R.id.owner)?.text = "Właściciel: ${ticket.owner}"
        view?.findViewById<TextView>(R.id.type)?.text = ticket.type
        view?.findViewById<TextView>(R.id.normalTicket)?.text = "Normalny: ${ticket.normalTicket}"
        view?.findViewById<TextView>(R.id.reducedTicket)?.text = "Ulgowy (76): ${ticket.reducedTicket}"
        view?.findViewById<TextView>(R.id.discount)?.text = "${ticket.discountCategory} ${ticket.discountPercent}"
        view?.findViewById<TextView>(R.id.carrier)?.text = "Przewoźnik: ${ticket.carrier}"
        view?.findViewById<TextView>(R.id.trainType)?.text = "Typ pociągu: ${ticket.trainType}"
        view?.findViewById<TextView>(R.id.price)?.text = "Cena: ${ticket.price} PLN"
        view?.findViewById<TextView>(R.id.travelDistance)?.text = "Distance: ${ticket.travelDistance} KM"
    }
}
