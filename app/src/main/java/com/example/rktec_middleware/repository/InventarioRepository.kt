package com.example.rktec_middleware.repository

import com.example.rktec_middleware.data.dao.InventarioDao
import com.example.rktec_middleware.data.model.ItemInventario
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventarioRepository @Inject constructor(private val inventarioDao: InventarioDao) {

    suspend fun listarTodosPorEmpresa(companyId: String): List<ItemInventario> =
        inventarioDao.listarTodosPorEmpresa(companyId)

    suspend fun buscarPorTag(tag: String, companyId: String): ItemInventario? =
        inventarioDao.buscarPorTag(tag, companyId)

    suspend fun corrigirSetor(epc: String, novoSetor: String, companyId: String) =
        inventarioDao.corrigirSetor(epc, novoSetor, companyId)

    suspend fun limparInventarioPorEmpresa(companyId: String) {
        inventarioDao.limparInventarioPorEmpresa(companyId)
    }

    suspend fun atualizarItem(item: ItemInventario) {
        inventarioDao.atualizarItem(item)
    }
}