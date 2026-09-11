package com.adrien.sgc

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class CadastroUsuarioActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_usuario)

        val edtNome = findViewById<TextInputEditText>(R.id.edtCadastroNome)
        val edtEmail = findViewById<TextInputEditText>(R.id.edtCadastroEmail)
        val edtSenha = findViewById<TextInputEditText>(R.id.edtCadastroSenha)
        val btnCadastrar = findViewById<MaterialButton>(R.id.btnRealizarCadastro)

        btnCadastrar.setOnClickListener {
            val nome = edtNome.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val senha = edtSenha.text.toString().trim()

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senha.length < 8) {
                Toast.makeText(this, "A senha deve ter pelo menos 8 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener {
                    Toast.makeText(this, "Vendedor cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { erro ->
                    Toast.makeText(this, "Erro: ${erro.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}