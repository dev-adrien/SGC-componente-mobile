package com.adrien.sgc

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class EstoqueActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val listaProdutos = mutableListOf<Produto>()
    private lateinit var adapter: ProdutoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estoque)

        val rvProdutos = findViewById<RecyclerView>(R.id.rvProdutos)
        rvProdutos.layoutManager = LinearLayoutManager(this)

        adapter = ProdutoAdapter(
            listaProdutos,
            onVerCodigoClick = { produto ->
                val intent = Intent(this, CodigoBarrasActivity::class.java).apply {
                    putExtra("PRODUTO_ID", produto.id)
                    putExtra("PRODUTO_NOME", produto.nome)
                    putExtra("PRODUTO_EAN", produto.codigoEan)
                }
                startActivity(intent)
            },
            onEditarClick = { produto ->
                exibirDialogEditarProduto(produto)
            }
        )

        rvProdutos.adapter = adapter
        carregarProdutos()
    }

    private fun carregarProdutos() {
        db.collection("produtos").get().addOnSuccessListener { snapshot ->
            listaProdutos.clear()
            for (doc in snapshot) {
                val produto = doc.toObject(Produto::class.java)
                produto.id = doc.id
                listaProdutos.add(produto)
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener { erro ->
            Toast.makeText(this, "Erro ao carregar: ${erro.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exibirDialogEditarProduto(produto: Produto) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_editar_produto, null)
        val edtNome = view.findViewById<TextInputEditText>(R.id.edtDialogNome)
        val edtPreco = view.findViewById<TextInputEditText>(R.id.edtDialogPreco)
        val edtEstoque = view.findViewById<TextInputEditText>(R.id.edtDialogEstoque)

        edtNome.setText(produto.nome)
        edtPreco.setText(produto.preco.toString())
        edtEstoque.setText(produto.quantidadeEstoque.toString())

        AlertDialog.Builder(this)
            .setTitle("Editar Produto")
            .setView(view)
            .setPositiveButton("Salvar") { _, _ ->
                val novoNome = edtNome.text.toString().trim()
                val novoPreco = edtPreco.text.toString().trim().toDoubleOrNull() ?: produto.preco
                val novoEstoque = edtEstoque.text.toString().trim().toIntOrNull() ?: produto.quantidadeEstoque

                if (novoNome.isEmpty()) {
                    Toast.makeText(this, "O nome não pode ficar vazio.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updates = mapOf<String, Any>(
                    "nome" to novoNome,
                    "preco" to novoPreco,
                    "quantidadeEstoque" to novoEstoque
                )

                db.collection("produtos").document(produto.id)
                    .update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Produto atualizado!", Toast.LENGTH_SHORT).show()
                        carregarProdutos()
                    }
                    .addOnFailureListener { erro ->
                        Toast.makeText(this, "Erro ao atualizar: ${erro.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}