package com.example.rktec_middleware.util

import java.security.MessageDigest

object SenhaUtils {
    fun hashSenha(senha: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(senha.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
