package com.adrien.sgc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class ScannerContinuoActivity : AppCompatActivity() {

    private val eansConfirmados = arrayListOf<String>()
    private var emPausaModal = false
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private val requisitarPermissaoLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedida ->
        if (concedida) {
            iniciarCamera()
        } else {
            Toast.makeText(this, "Permissão de câmera é necessária para ler códigos.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner_continuo)

        val btnFechar = findViewById<ImageButton>(R.id.btnFecharScanner)
        btnFechar.setOnClickListener {
            finalizarScanner()
        }

        verificarPermissaoEIniciar()
    }

    private fun verificarPermissaoEIniciar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            iniciarCamera()
        } else {
            requisitarPermissaoLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun iniciarCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            processarFrameCamera(imageProxy)
                        }
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
            } catch (e: Exception) {
                Toast.makeText(this, "Falha ao iniciar câmera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processarFrameCamera(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && !emPausaModal) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty() && !emPausaModal) {
                        val eanDetectado = barcodes[0].rawValue ?: ""
                        if (eanDetectado.isNotEmpty()) {
                            emPausaModal = true
                            runOnUiThread { exibirModalConfirmacao(eanDetectado) }
                        }
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }

    private fun exibirModalConfirmacao(ean: String) {
        AlertDialog.Builder(this)
            .setTitle("Código Detectado")
            .setMessage("Deseja adicionar o produto com código $ean?")
            .setPositiveButton("Adicionar") { _, _ ->
                eansConfirmados.add(ean)
                Toast.makeText(this, "Adicionado!", Toast.LENGTH_SHORT).show()
                emPausaModal = false
            }
            .setNegativeButton("Cancelar") { _, _ ->
                Toast.makeText(this, "Cancelado.", Toast.LENGTH_SHORT).show()
                emPausaModal = false
            }
            .setCancelable(false)
            .show()
    }

    private fun finalizarScanner() {
        val resultIntent = Intent().apply {
            putStringArrayListExtra("EANS_ESCANEADOS", eansConfirmados)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}