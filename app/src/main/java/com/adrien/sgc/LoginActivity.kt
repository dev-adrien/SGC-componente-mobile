package com.adrien.sgc

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val edtEmail = findViewById<TextInputEditText>(R.id.edtEmail)
        val edtSenha = findViewById<TextInputEditText>(R.id.edtSenha)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val txtCadastro = findViewById<TextView>(R.id.txtIrParaCadastro)

        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val senha = edtSenha.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha, vendedor!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener {
                    Toast.makeText(this, "Acesso liberado ao PDV!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { erro ->
                    Toast.makeText(this, "Falha no login: ${erro.message}", Toast.LENGTH_LONG).show()
                }
        }

        txtCadastro.setOnClickListener {
            val intent = Intent(this, CadastroUsuarioActivity::class.java)
            startActivity(intent)
        }
    }
}