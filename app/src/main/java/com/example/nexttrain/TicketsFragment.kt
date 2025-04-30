package com.example.nexttrain

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
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
            val tempFile = File(requireContext().cacheDir, "temp_selected.pdf")
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Teraz mamy PDF jako File
            renderFirstPageFromPdf(tempFile)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to open PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderFirstPageFromPdf(pdfFile: File) {
        try {
            val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)

            if (pdfRenderer.pageCount > 0) {
                val page = pdfRenderer.openPage(0)

                val scale = 3
                val bitmap = Bitmap.createBitmap(page.width * scale, page.height * scale, Bitmap.Config.ARGB_8888)
                val rect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)

                page.render(bitmap, rect, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                detectAndCropQrCode(bitmap) // Tutaj zamiast od razu wrzucać do ImageView
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
}
