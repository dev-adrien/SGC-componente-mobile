package com.adrien.sgc

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class ProdutoAdapter(
    private val listaProdutos: List<Produto>,
    private val onVerCodigoClick: (Produto) -> Unit,
    private val onEditarClick: (Produto) -> Unit
) : RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder>() {

    class ProdutoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome: TextView = view.findViewById(R.id.txtNomeProduto)
        val txtPreco: TextView = view.findViewById(R.id.txtPrecoProduto)
        val txtEstoque: TextView = view.findViewById(R.id.txtEstoqueProduto)
        val txtCodigo: TextView = view.findViewById(R.id.txtCodigoBarras)
        val btnCopiarCodigo: MaterialButton = view.findViewById(R.id.btnCopiarCodigo)
        val btnEditarProduto: MaterialButton = view.findViewById(R.id.btnEditarProduto)
        val btnVerCodigo: MaterialButton = view.findViewById(R.id.btnVerCodigo)
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

        val codigoConteudo = if (produto.codigoEan.isNotEmpty()) produto.codigoEan else produto.id
        holder.txtCodigo.text = "Cód/ID: $codigoConteudo"

        val copiarParaClipboard: (String) -> Unit = { valor ->
            val clipboard = holder.itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Código do Produto", valor)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(holder.itemView.context, "Código copiado: $valor", Toast.LENGTH_SHORT).show()
        }

        holder.btnCopiarCodigo.setOnClickListener { copiarParaClipboard(codigoConteudo) }
        holder.txtCodigo.setOnClickListener { copiarParaClipboard(codigoConteudo) }

        holder.btnEditarProduto.setOnClickListener { onEditarClick(produto) }
        holder.btnVerCodigo.setOnClickListener { onVerCodigoClick(produto) }
    }

    override fun getItemCount(): Int = listaProdutos.size
}