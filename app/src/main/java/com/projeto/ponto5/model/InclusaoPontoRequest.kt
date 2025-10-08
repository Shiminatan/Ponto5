package com.projeto.ponto5.model

data class InclusaoPontoRequest(
    val cpf: String,
    val latitude: Double,
    val longitude: Double,
    val precisao: Double,
    val dataHora: String
)
