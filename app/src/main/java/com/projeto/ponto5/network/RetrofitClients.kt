package com.projeto.ponto5.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClients {

    val auth: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://autenticador.secullum.com.br/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    val ponto: PontoApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://pontowebintegracaoexterna.secullum.com.br/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PontoApi::class.java)
    }
}
