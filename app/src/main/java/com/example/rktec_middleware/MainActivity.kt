package com.example.rktec_middleware

import android.os.Bundle
import android.view.KeyEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rktec_middleware.model.EpcTag
import com.example.rktec_middleware.ui.screens.EpcTagAdapter

class MainActivity : AppCompatActivity() {

    private val tags = mutableListOf<EpcTag>()
    private lateinit var adapter: EpcTagAdapter
    private lateinit var tvStatusLeitura: TextView
    private lateinit var tvContadorTags: TextView
    private lateinit var tvNenhumaTag: TextView

    private var lendo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leitura_rfid)

        tvStatusLeitura = findViewById(R.id.tvStatusLeitura)
        tvContadorTags = findViewById(R.id.tvContadorTags)
        tvNenhumaTag = findViewById(R.id.tvNenhumaTag)

        val recyclerView = findViewById<RecyclerView>(R.id.rvTagsLidas)
        adapter = EpcTagAdapter(tags)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        atualizarLista()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 139 && !lendo) {
            lendo = true
            tvStatusLeitura.text = "Leitura sendo efetuada..."
            // aqui chama seu service: rfidService.iniciarLeitura()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 139 && lendo) {
            lendo = false
            tvStatusLeitura.text = "Pressione o gatilho para ler"
            // aqui chama seu service: rfidService.pararLeitura()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    // Chame esse método sempre que adicionar tag
    fun adicionarTag(epc: String) {
        tags.add(EpcTag(epc))
        atualizarLista()
    }

    private fun atualizarLista() {
        tvContadorTags.text = "Total de tags lidas: ${tags.size}"
        tvNenhumaTag.visibility = if (tags.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        adapter.notifyDataSetChanged()
    }
}
