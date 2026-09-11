package com.adrien.sgc

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class HistoricoVendasAdapter(
    private val listaVendas: List<Venda>
) : RecyclerView.Adapter<HistoricoVendasAdapter.VendaViewHolder>() {

    class VendaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtStatus: TextView = view.findViewById(R.id.txtStatusVenda)
        val txtData: TextView = view.findViewById(R.id.txtDataVenda)
        val txtId: TextView = view.findViewById(R.id.txtIdVenda)
        val txtItens: TextView = view.findViewById(R.id.txtResumoItens)
        val txtTotal: TextView = view.findViewById(R.id.txtValorTotalVenda)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VendaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_venda_historico, parent, false)
        return VendaViewHolder(view)
    }

    override fun onBindViewHolder(holder: VendaViewHolder, position: Int) {
        val venda = listaVendas[position]
        val formatadorMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val formatadorData = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

        holder.txtId.text = "ID: ${venda.id}"
        holder.txtData.text = venda.data?.let { formatadorData.format(it) } ?: "Data indisponível"
        holder.txtTotal.text = formatadorMoeda.format(venda.valorTotal)

        val resumo = venda.itens.joinToString(separator = "\n") { item ->
            "• ${item.quantidade}x ${item.nome} (${formatadorMoeda.format(item.precoUnitario)})"
        }
        holder.txtItens.text = resumo.ifEmpty { "Sem detalhes dos itens" }

        val status = venda.status.uppercase()
        holder.txtStatus.text = status
        if (status == "CANCELADA") {
            holder.txtStatus.setTextColor(Color.parseColor("#C62828"))
            holder.txtStatus.setBackgroundColor(Color.parseColor("#FFEBEE"))
        } else {
            holder.txtStatus.setTextColor(Color.parseColor("#2E7D32"))
            holder.txtStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))
        }
    }

    override fun getItemCount(): Int = listaVendas.size
}