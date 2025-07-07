package com.example.rktec_middleware.repository

import com.example.rktec_middleware.data.dao.UsuarioDao
import com.example.rktec_middleware.data.model.Usuario


class UsuarioRepository(private val usuarioDao: UsuarioDao) {
    suspend fun inserir(usuario: Usuario) = usuarioDao.inserir(usuario)
    suspend fun atualizar(usuario: Usuario) = usuarioDao.atualizar(usuario)
    suspend fun buscarPorEmail(email: String) = usuarioDao.buscarPorEmail(email)
    suspend fun buscarPorNome(nome: String) = usuarioDao.buscarPorNome(nome)
    suspend fun listarTodos() = usuarioDao.listarTodos()
}

