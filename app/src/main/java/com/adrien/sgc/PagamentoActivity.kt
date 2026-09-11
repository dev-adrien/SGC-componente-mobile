package com.adrien.sgc

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

class PagamentoActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val chavePixEmail = "adrienbzr@outlook.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagamento)

        @Suppress("UNCHECKED_CAST")
        val itensVenda = intent.getSerializableExtra("ITENS_VENDA") as? ArrayList<ItemVenda> ?: arrayListOf()
        val total = intent.getDoubleExtra("VALOR_TOTAL", 0.0)

        val txtValor = findViewById<TextView>(R.id.txtValorFinalPagamento)
        val txtChave = findViewById<TextView>(R.id.txtChavePix)
        val btnFinalizar = findViewById<MaterialButton>(R.id.btnFinalizarVenda)

        txtChave.text = chavePixEmail
        txtValor.text = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(total)

        btnFinalizar.setOnClickListener {
            btnFinalizar.isEnabled = false
            efetivarBaixaNoEstoqueERegistrarVenda(itensVenda, total)
        }
    }

    private fun efetivarBaixaNoEstoqueERegistrarVenda(itens: List<ItemVenda>, total: Double) {
        db.runTransaction { transaction ->
            val atualizacoesEstoque = mutableListOf<Pair<DocumentReference, Long>>()

            for (item in itens) {
                val produtoRef = db.collection("produtos").document(item.produtoId)
                val snapshot = transaction.get(produtoRef)

                if (!snapshot.exists()) {
                    throw Exception("Produto '${item.nome}' não encontrado no banco.")
                }

                val estoqueAtual = snapshot.getLong("quantidadeEstoque") ?: 0
                if (estoqueAtual < item.quantidade) {
                    throw Exception("Estoque insuficiente para '${item.nome}'. Disponível: $estoqueAtual")
                }

                val novoEstoque = estoqueAtual - item.quantidade
                atualizacoesEstoque.add(Pair(produtoRef, novoEstoque))
            }

            for ((ref, novoEstoque) in atualizacoesEstoque) {
                transaction.update(ref, "quantidadeEstoque", novoEstoque)
            }

            val vendaRef = db.collection("vendas").document()
            val novaVenda = Venda(
                id = vendaRef.id,
                data = Date(),
                itens = itens,
                valorTotal = total,
                status = "CONCLUIDA",
                chavePix = chavePixEmail
            )
            transaction.set(vendaRef, novaVenda)

            null
        }.addOnSuccessListener {
            Toast.makeText(this, "Venda finalizada com sucesso!", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            findViewById<MaterialButton>(R.id.btnFinalizarVenda).isEnabled = true
        }
    }
}