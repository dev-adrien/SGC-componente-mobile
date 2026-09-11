package com.adrien.sgc

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val produtosDisponiveis = mutableListOf<Produto>()
    private val carrinho = mutableListOf<ItemVenda>()

    private lateinit var adapterCarrinho: CarrinhoAdapter
    private lateinit var txtTotal: TextView
    private lateinit var actvBusca: AutoCompleteTextView

    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val listaEans = result.data?.getStringArrayListExtra("EANS_ESCANEADOS") ?: arrayListOf()
            processarEansEscaneados(listaEans)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        txtTotal = findViewById(R.id.txtTotalVenda)
        actvBusca = findViewById(R.id.actvBuscarProduto)
        val rvCarrinho = findViewById<RecyclerView>(R.id.rvCarrinho)
        val btnScanner = findViewById<MaterialButton>(R.id.btnScannerCamera)
        val btnCancelar = findViewById<MaterialButton>(R.id.btnCancelarVenda)
        val btnPagamento = findViewById<MaterialButton>(R.id.btnIrPagamento)

        adapterCarrinho = CarrinhoAdapter(carrinho) { atualizarTotal() }
        rvCarrinho.layoutManager = LinearLayoutManager(this)
        rvCarrinho.adapter = adapterCarrinho

        carregarCatalogoProdutos()

        btnScanner.setOnClickListener {
            val intent = Intent(this, ScannerContinuoActivity::class.java)
            scannerLauncher.launch(intent)
        }

        btnCancelar.setOnClickListener {
            if (carrinho.isEmpty()) {
                finish()
                return@setOnClickListener
            }
            salvarVendaCancelada()
        }

        btnPagamento.setOnClickListener {
            if (carrinho.isEmpty()) {
                Toast.makeText(this, "Adicione itens ao carrinho primeiro!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, PagamentoActivity::class.java).apply {
                putExtra("ITENS_VENDA", ArrayList(carrinho))
                putExtra("VALOR_TOTAL", calcularTotal())
            }
            startActivity(intent)
        }
    }

    private fun carregarCatalogoProdutos() {
        db.collection("produtos").get().addOnSuccessListener { snapshot ->
            produtosDisponiveis.clear()
            val labels = mutableListOf<String>()

            for (doc in snapshot) {
                val prod = doc.toObject(Produto::class.java)
                prod.id = doc.id
                produtosDisponiveis.add(prod)
                labels.add("${prod.nome} (ID: ${prod.id})")
            }

            val adapterBusca = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, labels)
            actvBusca.setAdapter(adapterBusca)

            actvBusca.setOnItemClickListener { _, _, position, _ ->
                val labelSelecionado = adapterBusca.getItem(position) ?: ""
                val produto = produtosDisponiveis.find { "${it.nome} (ID: ${it.id})" == labelSelecionado }
                produto?.let { adicionarAoCarrinho(it) }
                actvBusca.setText("")
            }
        }
    }

    private fun adicionarAoCarrinho(produto: Produto) {
        if (produto.quantidadeEstoque <= 0) {
            Toast.makeText(this, "Produto '${produto.nome}' está esgotado!", Toast.LENGTH_SHORT).show()
            return
        }

        val itemExistente = carrinho.find { it.produtoId == produto.id }
        if (itemExistente != null) {
            if (itemExistente.quantidade + 1 > produto.quantidadeEstoque) {
                Toast.makeText(
                    this,
                    "Limite atingido! Estoque disponível: ${produto.quantidadeEstoque}",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            itemExistente.quantidade++
        } else {
            carrinho.add(
                ItemVenda(
                    produtoId = produto.id,
                    nome = produto.nome,
                    precoUnitario = produto.preco,
                    quantidade = 1
                )
            )
        }
        adapterCarrinho.notifyDataSetChanged()
        atualizarTotal()
    }

    private fun processarEansEscaneados(eans: List<String>) {
        for (ean in eans) {
            val produto = produtosDisponiveis.find { it.codigoEan == ean || it.id == ean }
            if (produto != null) {
                adicionarAoCarrinho(produto)
            } else {
                Toast.makeText(this, "Código não cadastrado: $ean", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calcularTotal(): Double = carrinho.sumOf { it.subtotal }

    private fun atualizarTotal() {
        val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        txtTotal.text = formatador.format(calcularTotal())
    }

    private fun salvarVendaCancelada() {
        val vendaCancelada = Venda(
            id = db.collection("vendas").document().id,
            data = Date(),
            itens = carrinho,
            valorTotal = calcularTotal(),
            status = "CANCELADA"
        )

        db.collection("vendas").add(vendaCancelada).addOnSuccessListener {
            Toast.makeText(this, "Venda cancelada registrada no histórico.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}