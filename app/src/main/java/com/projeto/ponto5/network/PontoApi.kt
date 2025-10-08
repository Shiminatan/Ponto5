package com.projeto.ponto5.network

import com.projeto.ponto5.model.InclusaoPontoRequest
import retrofit2.Response
import retrofit2.http.*

interface PontoApi {
    @POST("IntegracaoExterna/InclusaoPonto/Incluir")
    suspend fun incluirPonto(
        @Header("Authorization") token: String,
        @Header("secullumidbancoselecionado") idBanco: String,
        @Body body: InclusaoPontoRequest
    ): Response<Unit>
}
