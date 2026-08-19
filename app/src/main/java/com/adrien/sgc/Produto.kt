package com.adrien.sgc

import java.io.Serializable

data class Produto(
    var id: String = "",
    var nome: String = "",
    var preco: Double = 0.0,
    var codigoEan: String = "",
    var quantidadeEstoque: Int = 0
) : Serializable