package com.example.rktec_middleware.repository

import com.example.rktec_middleware.data.dao.InventarioDao
import com.example.rktec_middleware.data.model.ItemInventario
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventarioRepository @Inject constructor(private val inventarioDao: InventarioDao) {

    suspend fun listarTodos(): List<ItemInventario> = inventarioDao.listarTodos()

    suspend fun buscarPorTag(tag: String): ItemInventario? = inventarioDao.buscarPorTag(tag)

    suspend fun corrigirSetor(epc: String, novoSetor: String) = inventarioDao.corrigirSetor(epc, novoSetor)

    suspend fun limparInventario() {
        inventarioDao.limparInventario()
    }

    suspend fun atualizarItem(item: ItemInventario) {
        inventarioDao.atualizarItem(item)
    }
}