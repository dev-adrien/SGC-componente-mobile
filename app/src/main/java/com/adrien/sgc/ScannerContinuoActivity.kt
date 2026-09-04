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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class ScannerContinuoActivity : AppCompatActivity() {

    private val eansConfirmados = arrayListOf<String>()
    private var emPausaModal = false
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val db = FirebaseFirestore.getInstance()

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
                            verificarEstoqueEExibirModal(eanDetectado)
                        }
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }

    private fun verificarEstoqueEExibirModal(ean: String) {
        // Busca primeiro pelo campo codigoEan
        db.collection("produtos")
            .whereEqualTo("codigoEan", ean)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents[0]
                    validarDocumentoEstoque(doc, ean)
                } else {
                    // Se não achou pelo campo, tenta pelo ID do documento
                    db.collection("produtos").document(ean).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                validarDocumentoEstoque(doc, ean)
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this, "Produto não cadastrado ($ean)", Toast.LENGTH_SHORT).show()
                                    emPausaModal = false
                                }
                            }
                        }
                        .addOnFailureListener {
                            runOnUiThread { emPausaModal = false }
                        }
                }
            }
            .addOnFailureListener {
                runOnUiThread { emPausaModal = false }
            }
    }

    private fun validarDocumentoEstoque(doc: com.google.firebase.firestore.DocumentSnapshot, ean: String) {
        val nome = doc.getString("nome") ?: "Produto"
        val estoque = doc.getLong("quantidadeEstoque") ?: 0
        val jaAdicionados = eansConfirmados.count { it == ean }

        runOnUiThread {
            if (estoque <= 0) {
                Toast.makeText(this, "'$nome' está esgotado!", Toast.LENGTH_SHORT).show()
                emPausaModal = false
            } else if (jaAdicionados >= estoque) {
                Toast.makeText(this, "Limite de estoque atingido para '$nome' ($estoque disp.)", Toast.LENGTH_SHORT).show()
                emPausaModal = false
            } else {
                exibirModalConfirmacao(ean, nome, estoque - jaAdicionados)
            }
        }
    }

    private fun exibirModalConfirmacao(ean: String, nome: String, disponivel: Long) {
        AlertDialog.Builder(this)
            .setTitle("Produto Detectado")
            .setMessage("$nome\nDisponível: $disponivel\n\nDeseja adicionar ao carrinho?")
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