package com.adrien.sgc

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.io.OutputStream

class CodigoBarrasActivity : AppCompatActivity() {

    private var bitmapCodigo: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_codigo_barras)

        val nome = intent.getStringExtra("PRODUTO_NOME") ?: "Produto"
        val ean = intent.getStringExtra("PRODUTO_EAN").orEmpty()
        val id = intent.getStringExtra("PRODUTO_ID").orEmpty()

        val codigoFinal = if (ean.isNotEmpty()) ean else id

        val txtNome = findViewById<TextView>(R.id.txtEtiquetaNome)
        val txtId = findViewById<TextView>(R.id.txtEtiquetaId)
        val imgCodigo = findViewById<ImageView>(R.id.imgCodigoBarras)
        val btnBaixar = findViewById<MaterialButton>(R.id.btnBaixarImagem)
        val btnVoltar = findViewById<MaterialButton>(R.id.btnVoltarEstoque)
        val cardEtiqueta = findViewById<View>(R.id.cardEtiqueta)

        txtNome.text = nome
        txtId.text = codigoFinal

        val formato = if (ean.length == 13 && ean.all { it.isDigit() }) {
            BarcodeFormat.EAN_13
        } else {
            BarcodeFormat.CODE_128
        }

        try {
            bitmapCodigo = gerarBitmapBarras(codigoFinal, formato, 800, 260)
            imgCodigo.setImageBitmap(bitmapCodigo)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao renderizar barras: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        btnVoltar.setOnClickListener {
            finish()
        }

        btnBaixar.setOnClickListener {
            val bitmapEtiqueta = capturarViewComoBitmap(cardEtiqueta)
            salvarImagemNaGaleria(bitmapEtiqueta, "etiqueta_${nome.replace(" ", "_")}")
        }
    }

    private fun gerarBitmapBarras(texto: String, formato: BarcodeFormat, largura: Int, altura: Int): Bitmap {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(texto, formato, largura, altura)
        val bmp = Bitmap.createBitmap(largura, altura, Bitmap.Config.RGB_565)
        for (x in 0 until largura) {
            for (y in 0 until altura) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    private fun capturarViewComoBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun salvarImagemNaGaleria(bitmap: Bitmap, nomeArquivo: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$nomeArquivo.png")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SGC_Etiquetas")
            }
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            val stream: OutputStream? = contentResolver.openOutputStream(uri)
            stream?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                Toast.makeText(this, "Etiqueta salva na galeria!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Falha ao exportar imagem.", Toast.LENGTH_SHORT).show()
        }
    }
}