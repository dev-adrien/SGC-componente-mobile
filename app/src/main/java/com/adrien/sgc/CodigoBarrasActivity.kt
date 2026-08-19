package com.adrien.sgc

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException

class CodigoBarrasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_codigo_barras)

        val txtNome = findViewById<TextView>(R.id.txtEtiquetaNome)
        val txtId = findViewById<TextView>(R.id.txtEtiquetaId)
        val imgBarras = findViewById<ImageView>(R.id.imgCodigoBarras)
        val btnVoltar = findViewById<MaterialButton>(R.id.btnVoltarEstoque)

        val produto = intent.getSerializableExtra("PRODUTO_SALVO") as? Produto

        if (produto != null) {
            txtNome.text = produto.nome
            txtId.text = produto.id

            try {
                val bitmap = gerarCodigoBarras(produto.id)
                imgBarras.setImageBitmap(bitmap)
            } catch (e: WriterException) {
                Toast.makeText(this, "Erro ao desenhar barras: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnVoltar.setOnClickListener {

            finish()
        }
    }

    private fun gerarCodigoBarras(conteudo: String): Bitmap {
        val largura = 800
        val altura = 400

        val bitMatrix = MultiFormatWriter().encode(
            conteudo,
            BarcodeFormat.CODE_128,
            largura,
            altura
        )

        val bitmap = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)

        for (x in 0 until largura) {
            for (y in 0 until altura) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}