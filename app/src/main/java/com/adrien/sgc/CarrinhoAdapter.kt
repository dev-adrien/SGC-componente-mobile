package com.adrien.sgc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class CarrinhoAdapter(
    private val itens: MutableList<ItemVenda>,
    private val onItemRemovido: () -> Unit
) : RecyclerView.Adapter<CarrinhoAdapter.CarrinhoViewHolder>() {

    class CarrinhoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome: TextView = view.findViewById(R.id.txtItemNome)
        val txtDetalhes: TextView = view.findViewById(R.id.txtItemDetalhes)
        val txtSubtotal: TextView = view.findViewById(R.id.txtItemSubtotal)
        val btnRemover: ImageButton = view.findViewById(R.id.btnRemoverItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarrinhoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrinho, parent, false)
        return CarrinhoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarrinhoViewHolder, position: Int) {
        val item = itens[position]
        val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        holder.txtNome.text = item.nome
        holder.txtDetalhes.text = "${item.quantidade}x ${formatador.format(item.precoUnitario)}"
        holder.txtSubtotal.text = formatador.format(item.subtotal)

        holder.btnRemover.setOnClickListener {
            val posAtual = holder.adapterPosition
            if (posAtual != RecyclerView.NO_POSITION) {
                itens.removeAt(posAtual)
                notifyItemRemoved(posAtual)
                onItemRemovido()
            }
        }
    }

    override fun getItemCount(): Int = itens.size
}