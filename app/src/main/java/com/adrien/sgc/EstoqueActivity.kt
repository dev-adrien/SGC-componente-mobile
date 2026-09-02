package com.adrien.sgc

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
        adapter = ProdutoAdapter(listaProdutos)
        rvProdutos.adapter = adapter

        carregarProdutosDoFirestore()
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