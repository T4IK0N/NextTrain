package com.example.nexttrain

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
//import android.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class TicketFragment : Fragment(R.layout.fragment_ticket) {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var qrImageView: ImageView
    private val pdfPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        uri?.let { handlePickedPdf(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // np. lista biletów albo coś innego

        qrImageView = view.findViewById(R.id.qrImageView)
        drawerLayout = view.findViewById(R.id.drawerLayout);
        navigationView = view.findViewById(R.id.nav_view);
        toolbar = view.findViewById(R.id.toolbar);

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_chose -> {
                    openPdfPicker()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }

//        burgerMenu implement
        (activity as? MainActivity)?.setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle((activity as? MainActivity), drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
//        val bottomNav = (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        drawerLayout.addDrawerListener(toggle)
//        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
//            var isHidden = false
//
//            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
//                if (!isHidden && slideOffset > 0f) {
//                    bottomNav?.visibility = GONE
//                    isHidden = true
//                }
//            }
//
//            override fun onDrawerOpened(drawerView: View) {}
//
//            override fun onDrawerClosed(drawerView: View) {
//                bottomNav?.visibility = VISIBLE
//                isHidden = false
//            }
//
//            override fun onDrawerStateChanged(newState: Int) {}
//        })

        toggle.syncState()

        //earlier ticket
        val savedTicket = File(requireContext().filesDir, "tickets/last_ticket.pdf")
        if (savedTicket.exists()) {
            renderFirstPageFromPdf(savedTicket)
        }
    }

    private fun openPdfPicker() { //is used but dumbass didn't see that g:65
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        }
        pdfPickerLauncher.launch(intent)
    }


    private fun handlePickedPdf(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val ticketsDir = File(requireContext().filesDir, "tickets")
            if (!ticketsDir.exists()) ticketsDir.mkdirs()

            val pdfFile = File(ticketsDir, "last_ticket.pdf")
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
                    val barcode = barcodes[0] // tylko pierwszy znaleziony

                    barcode.boundingBox?.let { boundingBox ->
                        try {
                            val padding = 15

                            val left = (boundingBox.left - padding).coerceAtLeast(0)
                            val top = (boundingBox.top - padding).coerceAtLeast(0)
                            val right = (boundingBox.right + padding).coerceAtMost(bitmap.width)
                            val bottom = (boundingBox.bottom + padding).coerceAtMost(bitmap.height)

                            val width = right - left
                            val height = bottom - top

                            val qrBitmap = Bitmap.createBitmap(bitmap, left, top, width, height)

                            qrImageView.setImageBitmap(qrBitmap)
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
        view?.findViewById<TextView>(R.id.discount)?.text = "${ticket.discountCategory} ${ticket.discountPercent}%"
        view?.findViewById<TextView>(R.id.carrier)?.text = "Przewoźnik: ${ticket.carrier}"
        view?.findViewById<TextView>(R.id.trainType)?.text = "Typ pociągu: ${ticket.trainType}"
        view?.findViewById<TextView>(R.id.price)?.text = "Cena: ${ticket.price} PLN"
        view?.findViewById<TextView>(R.id.travelDistance)?.text = "Dystans: ${ticket.travelDistance} KM"
    }
}
