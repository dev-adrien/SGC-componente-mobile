package com.adrien.sgc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class UploadNotaActivity : AppCompatActivity() {


    private var uriArquivoSelecionado: Uri? = null


    private val selecionarArquivoLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            uriArquivoSelecionado = uri
            val txtArquivo = findViewById<TextView>(R.id.txtArquivoSelecionado)
            txtArquivo.text = "Arquivo carregado com sucesso!"
            txtArquivo.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
        } else {
            Toast.makeText(this, "Nenhum arquivo foi selecionado.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_nota)

        val btnEscolher = findViewById<MaterialButton>(R.id.btnEscolherArquivo)
        val btnProcessar = findViewById<MaterialButton>(R.id.btnProcessarNota)

        btnEscolher.setOnClickListener {
            selecionarArquivoLauncher.launch(arrayOf("image/*", "application/pdf"))
        }

        btnProcessar.setOnClickListener {
            if (uriArquivoSelecionado == null) {
                Toast.makeText(this, "Vendedor, selecione a nota fiscal primeiro!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Preparando para analisar com IA...", Toast.LENGTH_LONG).show()
        }
    }
}