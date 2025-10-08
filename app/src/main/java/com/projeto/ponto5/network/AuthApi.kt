package com.projeto.ponto5.network

import com.projeto.ponto5.model.TokenResponse
import retrofit2.http.*

interface AuthApi {
    @FormUrlEncoded
    @POST("Token")
    suspend fun login(
        @Field("grant_type") grantType: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("client_id") clientId: String
    ): TokenResponse
}
