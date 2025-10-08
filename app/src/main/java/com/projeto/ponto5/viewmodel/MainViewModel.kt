package com.projeto.ponto5.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projeto.ponto5.BuildConfig
import com.projeto.ponto5.model.InclusaoPontoRequest
import com.projeto.ponto5.network.RetrofitClients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class MainViewModel : ViewModel() {

    private val _cpf = MutableStateFlow("")
    val cpf = _cpf.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token = _token.asStateFlow()

    private val _eventoResultado = MutableSharedFlow<String>()

    val eventoResultado = _eventoResultado.asSharedFlow()

    fun atualizarCpf(novoCpf: String) {
        _cpf.value = novoCpf
    }

    private fun atualizarResultado(msg: String) {
        viewModelScope.launch {
            _eventoResultado.emit(msg)
        }
    }


    private fun salvarToken(novoToken: String) {
        _token.value = novoToken
    }

    fun login() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClients.auth.login(
                    grantType = "password",
                    username = BuildConfig.USERNAME,
                    password = BuildConfig.PASSWORD,
                    clientId = "3"
                )
                salvarToken(response.access_token)
                atualizarResultado("Token recebido!")
            } catch (e: Exception) {
                atualizarResultado("Erro ao obter token: ${e.message}")
            }
        }
    }

    fun incluirPonto(latitude: Double, longitude: Double, precisao: Double) {
        val cpfAtual = cpf.value
        val tokenAtual = token.value ?: run {
            atualizarResultado("Token não disponível")
            return
        }

        val idBanco = BuildConfig.IDDATABASE

        val dataHoraAtual = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm",
            Locale.getDefault()
        ).format(Date())

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val body = InclusaoPontoRequest(
                    cpf = cpfAtual,
                    latitude = latitude,
                    longitude = longitude,
                    precisao = precisao,
                    dataHora = dataHoraAtual
                )

                val response = RetrofitClients.ponto.incluirPonto(
                    token = "Bearer $tokenAtual",
                    idBanco = idBanco,
                    body = body
                )

                if (response.isSuccessful) {
                    atualizarResultado("✅ Ponto incluído com sucesso!")
                } else {
                    atualizarResultado("❌ Erro: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                atualizarResultado("Erro: ${e.message}")
            }
        }
    }
}
