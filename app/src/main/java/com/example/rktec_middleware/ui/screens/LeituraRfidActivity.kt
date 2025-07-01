package com.example.rktec_middleware.ui.screens

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rktec_middleware.R
import com.example.rktec_middleware.model.EpcTag

class LeituraRfidActivity : AppCompatActivity() {
    private val tagList = mutableListOf<EpcTag>()
    private lateinit var adapter: EpcTagAdapter
    private var lendo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leitura_rfid)

        val tvStatusLeitura = findViewById<TextView>(R.id.tvStatusLeitura)
        val tvContadorTags = findViewById<TextView>(R.id.tvContadorTags)
        val tvNenhumaTag = findViewById<TextView>(R.id.tvNenhumaTag)
        val rvTagsLidas = findViewById<RecyclerView>(R.id.rvTagsLidas)

        adapter = EpcTagAdapter(tagList)
        rvTagsLidas.layoutManager = LinearLayoutManager(this)
        rvTagsLidas.adapter = adapter

        atualizaLista(tvContadorTags, tvNenhumaTag)
    }

    fun adicionarTag(epc: String, tvContadorTags: TextView, tvNenhumaTag: TextView) {
        tagList.add(EpcTag(epc))
        atualizaLista(tvContadorTags, tvNenhumaTag)
    }

    private fun atualizaLista(tvContadorTags: TextView, tvNenhumaTag: TextView) {
        tvContadorTags.text = "Total de tags lidas: ${tagList.size}"
        tvNenhumaTag.visibility = if (tagList.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        adapter.notifyDataSetChanged()
    }
}
