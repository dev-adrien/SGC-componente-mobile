package com.adrien.sgc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class ProdutoAdapter(private val listaProdutos: List<Produto>) :
    RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder>() {

    class ProdutoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome: TextView = view.findViewById(R.id.txtNomeProduto)
        val txtPreco: TextView = view.findViewById(R.id.txtPrecoProduto)
        val txtEstoque: TextView = view.findViewById(R.id.txtEstoqueProduto)
        val txtCodigo: TextView = view.findViewById(R.id.txtCodigoBarras)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto, parent, false)
        return ProdutoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val produto = listaProdutos[position]
        val formatadorMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        holder.txtNome.text = produto.nome
        holder.txtPreco.text = formatadorMoeda.format(produto.preco)
        holder.txtEstoque.text = "Qtd: ${produto.quantidadeEstoque}"

        holder.txtCodigo.text = if (produto.codigoEan.isNotEmpty()) {
            "Cód: ${produto.codigoEan}"
        } else {
            "ID: ${produto.id}"
        }
    }

    override fun getItemCount(): Int = listaProdutos.size
}