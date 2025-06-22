package com.example.nexttrain

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class TicketFragment : Fragment(R.layout.fragment_ticket) {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var qrImageView: ImageView
    private lateinit var ticketManager: TicketManager
    private val pdfPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        uri?.let { handlePickedPdf(it) }
    }
    private var isQrImageViewLarge = false // doubleTapQr flag
    private var originalBrightness: Float? = null // doubleTapQr flag

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // np. lista biletów albo coś innego

        ticketManager = TicketManager()
        drawerLayout = view.findViewById(R.id.drawerLayout);
        navigationView = view.findViewById(R.id.nav_view);
        toolbar = view.findViewById(R.id.toolbar);
        qrImageView = view.findViewById(R.id.qrImageView)
        qrImageView.scaleX = 1f
        qrImageView.scaleY = 1f

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

        // burgerMenu implement
        (activity as? MainActivity)?.setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle((activity as? MainActivity), drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)

        toggle.syncState()

        // earlier ticket
        val savedTicket = File(requireContext().filesDir, "tickets/last_ticket.pdf")
        if (savedTicket.exists()) {
            renderPageFromPdf(savedTicket)
        }

        // gesturedetector to detect double-tap
        val gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                doubleTapQr()
                return super.onDoubleTap(e)
            }
        })

        qrImageView.setOnTouchListener { _, event ->
            Log.e("GESTURE DETECTOR", "dziala")
            gestureDetector.onTouchEvent(event)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resetBrightnessToOriginal()
    }

    override fun onPause() {
        super.onPause()
        resetBrightnessToOriginal()
    }

    // pdf manager
    private fun openPdfPicker() {
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

            renderPageFromPdf(pdfFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to open PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderPageFromPdf(pdfFile: File) {
        try {
            PDFBoxResourceLoader.init(requireContext())

            val document = PDDocument.load(pdfFile)
            val pdfStripper = PDFTextStripper()
            val extractedTextFromPdf = pdfStripper.getText(document)

            val ticketText: String = extractedTextFromPdf // już wczytany wcześniej
            val ticket: Ticket = parseTicketText(ticketText)

            document.close()

            Log.e("Ticket From PDF", "Ticket: $ticket")
            Log.e("Ticket From PDF", extractedTextFromPdf)
            view?.let { ticketManager.updateTicketUI(ticket, it) }

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

    // qr from pdf manager
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
                            val padding = 20

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

    // parse there from class TicketManager
    private fun parseTicketText(text: String): Ticket {
        val carrierPolregio = Regex("""Przewoźnik:\s*(\S+)\s+""").find(text)?.groupValues?.get(1) ?: ""
        val carrierIntercity = Regex("""PRZEWOŹNIK/SPRZEDAWCA\s+([^\n]+)""").find(text)?.groupValues?.get(1)?.uppercase()?.trim() ?: ""
        return when {
            "INTERCITY" in carrierIntercity -> ticketManager.parseTicketIntercity(text)
            "POLREGIO" in carrierPolregio -> ticketManager.parseTicketPolregio(text)
            else -> Ticket()
        }
    }

    // double tap on qrImageView and brightness
    private fun doubleTapQr() {
        val targetScale = if (isQrImageViewLarge) 1f else 2.1f

        qrImageView.animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .setDuration(300)
            .start()

        if (!isQrImageViewLarge) {
            // save actual brightness and set max
            val window = requireActivity().window
            val lp = window.attributes

            if (originalBrightness == null) {
                originalBrightness = lp.screenBrightness.takeIf { it >= 0f } // tylko jeśli jest ustawiona
            }

            lp.screenBrightness = 1f // max
            window.attributes = lp
        } else {
            // reset brightness
            resetBrightnessToOriginal()
        }

        isQrImageViewLarge = !isQrImageViewLarge
    }

    private fun resetBrightnessToOriginal() {
        val window = activity?.window ?: return
        val lp = window.attributes

        lp.screenBrightness = originalBrightness ?: -1f // -1 -> system default
        window.attributes = lp
    }
}
