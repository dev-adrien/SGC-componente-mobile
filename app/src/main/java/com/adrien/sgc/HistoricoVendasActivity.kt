package com.adrien.sgc

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HistoricoVendasActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val listaVendas = mutableListOf<Venda>()
    private lateinit var adapter: HistoricoVendasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_vendas)

        val rvHistorico = findViewById<RecyclerView>(R.id.rvHistoricoVendas)
        adapter = HistoricoVendasAdapter(listaVendas)
        rvHistorico.layoutManager = LinearLayoutManager(this)
        rvHistorico.adapter = adapter

        carregarHistorico()
    }

    private fun carregarHistorico() {
        db.collection("vendas")
            .orderBy("data", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                listaVendas.clear()
                for (doc in snapshot) {
                    val venda = doc.toObject(Venda::class.java)
                    venda.id = doc.id
                    listaVendas.add(venda)
                }
                adapter.notifyDataSetChanged()
                if (listaVendas.isEmpty()) {
                    Toast.makeText(this, "Nenhuma venda registrada ainda.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar histórico: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}