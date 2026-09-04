package com.adrien.sgc

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class EstoqueActivity : AppCompatActivity() {

    private lateinit var rvProdutos: RecyclerView
    private lateinit var txtTotal: TextView
    private val listaProdutos = mutableListOf<Produto>()
    private lateinit var adapter: ProdutoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estoque)

        txtTotal = findViewById(R.id.txtTotalItens)
        rvProdutos = findViewById(R.id.rvProdutos)

        rvProdutos.layoutManager = LinearLayoutManager(this)

        carregarProdutosDoFirestore()

        adapter = ProdutoAdapter(listaProdutos) { produto ->
            val intent = Intent(this, CodigoBarrasActivity::class.java).apply {
                putExtra("PRODUTO_ID", produto.id)
                putExtra("PRODUTO_NOME", produto.nome)
                putExtra("PRODUTO_EAN", produto.codigoEan)
            }
            startActivity(intent)
        }
        rvProdutos.adapter = adapter
    }

    private fun carregarProdutosDoFirestore() {
        val db = FirebaseFirestore.getInstance()

        db.collection("produtos")
            .get()
            .addOnSuccessListener { result ->
                listaProdutos.clear()
                for (documento in result) {
                    val produto = documento.toObject(Produto::class.java)
                    produto.id = documento.id
                    listaProdutos.add(produto)
                }
                adapter.notifyDataSetChanged()
                txtTotal.text = "Total de itens cadastrados: ${listaProdutos.size}"
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao carregar estoque: ${exception.message}", Toast.LENGTH_SHORT).show()
                txtTotal.text = "Falha ao carregar itens."
            }
    }
}