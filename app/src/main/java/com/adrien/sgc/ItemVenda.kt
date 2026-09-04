package com.adrien.sgc

import java.io.Serializable

data class ItemVenda(
    var produtoId: String = "",
    var nome: String = "",
    var precoUnitario: Double = 0.0,
    var quantidade: Int = 1
) : Serializable {
    val subtotal: Double
        get() = precoUnitario * quantidade
}