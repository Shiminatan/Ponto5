package com.projeto.ponto5

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.projeto.ponto5.ui.theme.Ponto5Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.text.SimpleDateFormat
import java.util.*
import com.projeto.ponto5.BuildConfig

// -------------------- MODELOS --------------------
data class TokenResponse(val access_token: String)

data class InclusaoPontoRequest(
    val cpf: String,
    val latitude: Double,
    val longitude: Double,
    val precisao: Double,
    val dataHora: String
)

// -------------------- API --------------------
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

interface PontoApi {
    @POST("IntegracaoExterna/InclusaoPonto/Incluir")
    suspend fun incluirPonto(
        @Header("Authorization") token: String,
        @Header("secullumidbancoselecionado") idBanco: String,
        @Body body: InclusaoPontoRequest
    ): Response<Unit>
}

// -------------------- RETROFIT --------------------
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

// -------------------- MAIN --------------------
class MainActivity : ComponentActivity() {
    private var token: String? = null
    private val idBanco = BuildConfig.IDDATABASE // <- coloque aqui o ID real

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Ao abrir o app, já faz login e pega o token
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClients.auth.login(
                    grantType = "password",
                    username = BuildConfig.USERNAME,
                    password = BuildConfig.PASSWORD,
                    clientId = "3"
                )
                token = response.access_token
                Log.d("API", "Token recebido: $token")
            } catch (e: Exception) {
                Log.e("API", "Erro ao obter token: ${e.message}")
            }
        }

        setContent {
            Ponto5Theme {
                var cpf by remember { mutableStateOf("") }
                var resultado by remember { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = cpf,
                        onValueChange = { cpf = it },
                        label = { Text("CPF") }
                    )

                    Button(onClick = {
                        if (token.isNullOrEmpty()) {
                            resultado = "Token não disponível ainda"
                            return@Button
                        }

                        val dataHoraAtual = SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm",
                            Locale.getDefault()
                        ).format(Date())

                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val body = InclusaoPontoRequest(
                                    cpf = cpf,
                                    latitude = -29.656351437469443,
                                    longitude = -51.051604049630456,
                                    precisao = 10.0,
                                    dataHora = dataHoraAtual
                                )

                                val response = RetrofitClients.ponto.incluirPonto(
                                    token = "Bearer $token",
                                    idBanco = idBanco,
                                    body = body
                                )

                                resultado = if (response.isSuccessful) {
                                    "✅ Ponto incluído com sucesso!"
                                } else {
                                    "❌ Erro: ${response.code()} - ${response.message()}"
                                }
                            } catch (e: Exception) {
                                resultado = "Erro: ${e.message}"
                            }
                        }
                    }) {
                        Text("Incluir Ponto")
                    }

                    if (resultado.isNotEmpty()) {
                        Text(resultado, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
