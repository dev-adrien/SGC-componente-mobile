package com.adrien.sgc

import java.io.Serializable
import java.util.Date

data class Venda(
    var id: String = "",
    var data: Date = Date(),
    var itens: List<ItemVenda> = emptyList(),
    var valorTotal: Double = 0.0,
    var status: String = "CONCLUIDA",
    var chavePix: String = ""
) : Serializable