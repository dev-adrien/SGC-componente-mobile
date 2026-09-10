package com.adrien.sgc

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CadastroProdutoActivity : AppCompatActivity() {

    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_produto)

        val edtCodigo = findViewById<TextInputEditText>(R.id.edtProdutoCodigo)
        val edtNome = findViewById<TextInputEditText>(R.id.edtProdutoNome)
        val edtPreco = findViewById<TextInputEditText>(R.id.edtProdutoPreco)
        val edtEstoque = findViewById<TextInputEditText>(R.id.edtProdutoEstoque)
        val btnSalvar = findViewById<MaterialButton>(R.id.btnSalvarProduto)

        btnSalvar.setOnClickListener {
            val codigo = edtCodigo.text.toString().trim()
            val nome = edtNome.text.toString().trim()
            val precoTexto = edtPreco.text.toString().trim()
            val estoqueTexto = edtEstoque.text.toString().trim()

            if (nome.isEmpty() || precoTexto.isEmpty() || estoqueTexto.isEmpty()) {
                Toast.makeText(this, "Preencha nome, preço e quantidade!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val preco = precoTexto.toDoubleOrNull() ?: 0.0
            val estoque = estoqueTexto.toIntOrNull() ?: 0

            btnSalvar.isEnabled = false
            processarProduto(codigo, nome, preco, estoque) {
                btnSalvar.isEnabled = true
            }
        }
    }

    private fun processarProduto(
        codigo: String,
        nome: String,
        preco: Double,
        quantidadeAdicional: Int,
        onConcluido: () -> Unit
    ) {
        val colecao = db.collection("produtos")

        // 1. Se informou código, verifica por código primeiro
        if (codigo.isNotEmpty()) {
            colecao.whereEqualTo("codigoEan", codigo).limit(1).get()
                .addOnSuccessListener { query ->
                    if (!query.isEmpty) {
                        atualizarEstoque(query.documents[0], nome, preco, quantidadeAdicional, codigo)
                    } else {
                        buscarPorNome(nome, preco, quantidadeAdicional, codigo, onConcluido)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro na verificação: ${e.message}", Toast.LENGTH_SHORT).show()
                    onConcluido()
                }
        } else {
            buscarPorNome(nome, preco, quantidadeAdicional, "", onConcluido)
        }
    }

    private fun buscarPorNome(
        nome: String,
        preco: Double,
        quantidadeAdicional: Int,
        codigo: String,
        onConcluido: () -> Unit
    ) {
        db.collection("produtos")
            .whereEqualTo("nome", nome)
            .limit(1)
            .get()
            .addOnSuccessListener { query ->
                if (!query.isEmpty) {
                    atualizarEstoque(query.documents[0], nome, preco, quantidadeAdicional, codigo)
                } else {
                    cadastrarNovo(nome, preco, quantidadeAdicional, codigo, onConcluido)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro na verificação: ${e.message}", Toast.LENGTH_SHORT).show()
                onConcluido()
            }
    }

    private fun atualizarEstoque(
        doc: DocumentSnapshot,
        nome: String,
        preco: Double,
        quantidadeAdicional: Int,
        codigo: String
    ) {
        val updates = mutableMapOf<String, Any>(
            "quantidadeEstoque" to FieldValue.increment(quantidadeAdicional.toLong()),
            "preco" to preco
        )

        val eanExistente = doc.getString("codigoEan").orEmpty()
        val eanFinal = if (eanExistente.isNotEmpty()) eanExistente else codigo
        if (eanExistente.isEmpty() && codigo.isNotEmpty()) {
            updates["codigoEan"] = codigo
        }

        doc.reference.update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Estoque de '$nome' atualizado (+${quantidadeAdicional})!", Toast.LENGTH_SHORT).show()
                abrirCodigoBarras(doc.id, nome, eanFinal)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao atualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cadastrarNovo(
        nome: String,
        preco: Double,
        quantidade: Int,
        codigo: String,
        onConcluido: () -> Unit
    ) {
        val novoProduto = Produto(
            id = "",
            nome = nome,
            preco = preco,
            codigoEan = codigo,
            quantidadeEstoque = quantidade
        )

        db.collection("produtos").add(novoProduto)
            .addOnSuccessListener { docRef ->
                val idGerado = docRef.id
                docRef.update("id", idGerado).addOnSuccessListener {
                    Toast.makeText(this, "Produto cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                    abrirCodigoBarras(idGerado, nome, codigo)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao cadastrar: ${e.message}", Toast.LENGTH_SHORT).show()
                onConcluido()
            }
    }

    private fun abrirCodigoBarras(id: String, nome: String, codigoEan: String) {
        val intent = Intent(this, CodigoBarrasActivity::class.java).apply {
            putExtra("PRODUTO_ID", id)
            putExtra("PRODUTO_NOME", nome)
            putExtra("PRODUTO_EAN", codigoEan)
        }
        startActivity(intent)
        finish()
    }
}