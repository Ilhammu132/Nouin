package com.project.tokoai.ui.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.project.tokoai.R

/*
Sebuah Class untuk menampilkan list data AI pada listView yang ada didalan data paket Item Bundling
yang dipanggil di Item Adapter pada ViewHolder milik Item Bundling
 */
class ItemNamePackageAdapter(
    private val context: Activity,
    private val aiNameList: List<String>,
    private val aiPriceList: List<Double>
) :
    ArrayAdapter<String?>(context, R.layout.item_listview_package, aiNameList) {
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView: View = inflater.inflate(R.layout.item_listview_package, null, true)
        val aiName = rowView.findViewById<View>(R.id.tv_ai_name) as TextView
        val aiPrice = rowView.findViewById<View>(R.id.tv_ai_price) as TextView
        aiName.text = aiNameList[position]
        aiPrice.text = StringBuilder("${aiPriceList[position]}$")
        return rowView
    }
}