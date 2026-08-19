package com.adrien.sgc

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class CadastroProdutoActivity : AppCompatActivity() {

    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_produto)

        val edtNome = findViewById<TextInputEditText>(R.id.edtProdutoNome)
        val edtPreco = findViewById<TextInputEditText>(R.id.edtProdutoPreco)
        val edtEstoque = findViewById<TextInputEditText>(R.id.edtProdutoEstoque)
        val btnSalvar = findViewById<MaterialButton>(R.id.btnSalvarProduto)

        btnSalvar.setOnClickListener {
            val nome = edtNome.text.toString().trim()
            val precoTexto = edtPreco.text.toString().trim()
            val estoqueTexto = edtEstoque.text.toString().trim()

            if (nome.isEmpty() || precoTexto.isEmpty() || estoqueTexto.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos do produto!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val preco = precoTexto.toDoubleOrNull() ?: 0.0
            val estoque = estoqueTexto.toIntOrNull() ?: 0

            val produto = Produto(
                id = "",
                nome = nome,
                preco = preco,
                codigoEan = "",
                quantidadeEstoque = estoque
            )

            db.collection("produtos")
                .add(produto)
                .addOnSuccessListener { documento ->
                    val idGerado = documento.id

                    db.collection("produtos").document(idGerado)
                        .update("id", idGerado)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Produto salvo! Preparando código de barras...", Toast.LENGTH_SHORT).show()

                            produto.id = idGerado

                            val intent = Intent(
                                this@CadastroProdutoActivity,
                                CodigoBarrasActivity::class.java
                            )
                            intent.putExtra("PRODUTO_SALVO", produto)
                            startActivity(intent)

                            finish()
                        }
                }
                .addOnFailureListener { erro ->
                    Toast.makeText(this, "Erro ao salvar produto: ${erro.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}