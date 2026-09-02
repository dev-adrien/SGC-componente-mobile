package com.adrien.sgc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.launch
import com.google.firebase.ai.type.content

import org.json.JSONArray
import com.google.firebase.firestore.FirebaseFirestore

class UploadNotaActivity : AppCompatActivity() {


    private var uriArquivoSelecionado: Uri? = null

    private lateinit var btnProcessar: MaterialButton

    private val selecionarArquivoLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            uriArquivoSelecionado = uri
            val txtArquivo = findViewById<TextView>(R.id.txtArquivoSelecionado)
            txtArquivo.text = "Arquivo carregado com sucesso!"
            txtArquivo.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
        } else {
            Toast.makeText(this, "Nenhum arquivo foi selecionado.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_nota)

        val btnEscolher = findViewById<MaterialButton>(R.id.btnEscolherArquivo)

        btnProcessar = findViewById<MaterialButton>(R.id.btnProcessarNota)

        btnEscolher.setOnClickListener {
            selecionarArquivoLauncher.launch(arrayOf("image/*", "application/pdf"))
        }

        btnProcessar.setOnClickListener {
            val uri = uriArquivoSelecionado
            if (uri == null) {
                Toast.makeText(this, "selecione a nota fiscal para continuar!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            processarNotaComIA(uri)
        }
    }

    private fun processarNotaComIA(uri: Uri) {
        btnProcessar.isEnabled = false
        btnProcessar.text = "Analisando nota..."
        Toast.makeText(this, "Enviando arquivo para a Inteligência Artificial...", Toast.LENGTH_LONG).show()

        lifecycleScope.launch {
            try {
                val bytes = contentResolver.openInputStream(uri)?.readBytes()
                    ?: throw Exception("Falha ao ler o arquivo selecionado.")
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

                val modelo = Firebase.ai(backend = GenerativeBackend.googleAI())
                    .generativeModel("gemini-3.5-flash")

                val promptText = """
                    Atue como um sistema de automação de PDV. 
                    Analise a nota fiscal em anexo e extraia todos os produtos comprados.
                    Retorne APENAS um array JSON válido. NUNCA use formatação markdown (```json).
                    Cada objeto do array deve ter exatamente esta estrutura:
                    {
                        "nome": "Nome do Produto Formatado e Limpo",
                        "preco": 0.00,
                        "quantidade": 1,
                        "codigoEan": "1234567890123"
                    }
                    Se não encontrar o código de barras (EAN), deixe a string vazia "".
                    Se houver descontos no item, considere o preço final.
                """.trimIndent()

                val resposta = modelo.generateContent(
                    content {
                        inlineData(bytes = bytes, mimeType = mimeType)
                        text(promptText)
                    }
                )

                val textoJson = resposta.text?.trim().orEmpty()

                println("JSON BRUTO: $textoJson")
                processarJsonESalvar(textoJson)

            } catch(e: Exception) {
                Toast.makeText(this@UploadNotaActivity, "Erro ao processar: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                btnProcessar.isEnabled = true
                btnProcessar.text = "PROCESSAR COM IA"
            }
        }
    }

    private fun processarJsonESalvar(jsonRecebido: String) {
        val db = FirebaseFirestore.getInstance()

        try {

            val jsonArray = JSONArray(jsonRecebido)
            val totalItens = jsonArray.length()
            var itensProcessados = 0

            fun verificarConclusao() {
                itensProcessados++
                if (itensProcessados == totalItens) {
                    Toast.makeText(this, "Sucesso! $totalItens produtos adicionados ao estoque.", Toast.LENGTH_LONG).show()
                }
            }

            for (i in 0 until totalItens) {
                val item = jsonArray.getJSONObject(i)

                val nome = item.optString("nome", "Produto Desconhecido")
                val preco = item.optDouble("preco", 0.0)
                val quantidadeNova = item.optInt("quantidade", 1)
                val codigoEan = item.optString("codigoEan", "")

                db.collection("produtos")
                    .whereEqualTo("nome", nome)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val docExistente = querySnapshot.documents[0]
                            val qtdAtual = docExistente.getLong("quantidadeEstoque") ?: 0

                            docExistente.reference.update(
                                "quantidadeEstoque", qtdAtual + quantidadeNova,
                                "preco", preco
                            ).addOnCompleteListener { verificarConclusao() }
                        } else {
                            val produtoNovoMap = hashMapOf(
                                "nome" to nome,
                                "preco" to preco,
                                "quantidadeEstoque" to quantidadeNova,
                                "codigoBarras" to codigoEan
                            )

                            db.collection("produtos").add(produtoNovoMap)
                                .addOnCompleteListener { verificarConclusao() }
                        }
                    }
                    .addOnFailureListener { e ->
                        println("Erro ao buscar produto: ${e.message}")
                        verificarConclusao()
                    }

            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao interpretar os dados da nota: ${e.message}", Toast.LENGTH_LONG).show()

            println("Erro JSON: ${e.message}")
        }
    }
}