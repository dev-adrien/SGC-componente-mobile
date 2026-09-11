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

            val quantidade = estoqueTexto.toIntOrNull()
            if (quantidade == null || quantidade <= 0) {
                Toast.makeText(this, "Informe uma quantidade válida!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Fluxo 1: Usuário informou Código EAN ou ID
            if (codigo.isNotEmpty()) {
                btnSalvar.isEnabled = false
                processarPorCodigoOuId(codigo, nome, precoTexto, quantidade) {
                    btnSalvar.isEnabled = true
                }
                return@setOnClickListener
            }

            // Fluxo 2: Cadastro novo sem código
            val preco = precoTexto.toDoubleOrNull()
            if (nome.isEmpty() || preco == null) {
                Toast.makeText(this, "Para cadastrar sem código, preencha Nome e Preço!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSalvar.isEnabled = false
            cadastrarNovoProduto(nome, preco, quantidade, codigo = "") {
                btnSalvar.isEnabled = true
            }
        }
    }

    private fun processarPorCodigoOuId(
        codigo: String,
        nome: String,
        precoTexto: String,
        quantidade: Int,
        onFinalizado: () -> Unit
    ) {
        val colecao = db.collection("produtos")

        // 1. Tenta encontrar por codigoEan
        colecao.whereEqualTo("codigoEan", codigo).limit(1).get()
            .addOnSuccessListener { query ->
                if (!query.isEmpty) {
                    incrementarEstoque(query.documents[0], quantidade, codigo)
                } else {
                    // 2. Se não achou por EAN, tenta encontrar diretamente pelo ID do documento
                    colecao.document(codigo).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                incrementarEstoque(doc, quantidade, doc.getString("codigoEan").orEmpty())
                            } else {
                                // Não encontrou nem por EAN nem por ID: trata como produto novo
                                val preco = precoTexto.toDoubleOrNull()
                                if (nome.isEmpty() || preco == null) {
                                    Toast.makeText(
                                        this,
                                        "Código/ID não encontrado! Preencha Nome e Preço para cadastrar este novo item.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onFinalizado()
                                } else {
                                    cadastrarNovoProduto(nome, preco, quantidade, codigo, onFinalizado)
                                }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Erro ao buscar por ID: ${it.message}", Toast.LENGTH_SHORT).show()
                            onFinalizado()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao consultar: ${e.message}", Toast.LENGTH_SHORT).show()
                onFinalizado()
            }
    }

    private fun incrementarEstoque(
        doc: DocumentSnapshot,
        quantidade: Int,
        codigoFinal: String
    ) {
        val nomeExistente = doc.getString("nome") ?: "Produto"

        doc.reference.update("quantidadeEstoque", FieldValue.increment(quantidade.toLong()))
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Estoque adicionado (+${quantidade}) em '$nomeExistente'!",
                    Toast.LENGTH_LONG
                ).show()
                abrirCodigoBarras(doc.id, nomeExistente, codigoFinal)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao somar estoque: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cadastrarNovoProduto(
        nome: String,
        preco: Double,
        quantidade: Int,
        codigo: String,
        onFinalizado: () -> Unit
    ) {
        val novoProduto = Produto(
            id = "",
            nome = nome,
            preco = preco,
            codigoEan = codigo,
            quantidadeEstoque = quantidade
        )

        db.collection("produtos")
            .add(novoProduto)
            .addOnSuccessListener { docRef ->
                val idGerado = docRef.id
                docRef.update("id", idGerado).addOnSuccessListener {
                    Toast.makeText(this, "Novo produto cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                    abrirCodigoBarras(idGerado, nome, codigo)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao cadastrar: ${e.message}", Toast.LENGTH_SHORT).show()
                onFinalizado()
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