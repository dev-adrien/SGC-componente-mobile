package com.adrien.sgc

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnManual = findViewById<MaterialButton>(R.id.btnMenuProdutoManual)
        val btnIA = findViewById<MaterialButton>(R.id.btnMenuNotaFiscal)

        btnManual.setOnClickListener {
            val intent = Intent(this, CadastroProdutoActivity::class.java)
            startActivity(intent)
        }

        btnIA.setOnClickListener {
            Toast.makeText(this, "Em breve: Leitura de Nota Fiscal com IA!", Toast.LENGTH_SHORT).show()
        }
    }
}