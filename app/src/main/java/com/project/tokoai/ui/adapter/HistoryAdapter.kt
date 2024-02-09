package com.project.tokoai.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.tokoai.R
import com.project.tokoai.model.Orders

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    // Variable dibawah ini digunakan untuk menampung list item History.
    private val listHistory: ArrayList<Orders> = ArrayList()

    /*
    Method buatan ini digunakan untuk mengisi variable ArrayList History diatas. Kosongkan terlebih
    dahulu variable nya baru tambahkan semua list dari parameter kedalam variable diatas.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setListHistory(itemList: ArrayList<Orders>) {
        listHistory.clear()
        listHistory.addAll(itemList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate layout per item dengan layout yang telah dibuat di direktori layout yaitu item_history_row.
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Set Data per item dengan method buatan pada Class ViewHolder dibawah.
        holder.setData(listHistory[position])
    }

    // Mengembalikan berapa jumlah item yang ditampilkan di RecyclerView. Jumlah akan fleksibel mengikuti size dari List History.
    override fun getItemCount() = listHistory.size

    /*
    Sebuah Class untuk menyambungkan dan/atau mengisi item-item view pada layout item_history_row sebelumnya.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Pendeklarasian variable view yang ada pada item_history_row
        private var tvHistoryTotal: TextView
        private var tvHistoryPurchased: TextView

        init {
            // Ketika Class pertama kali dipanngil maka variable yang dideklarasika diatas akan langsung diinisialisasi dengan id yang sesuai pada layout item_history_row
            tvHistoryTotal = itemView.findViewById(R.id.tv_history_price)
            tvHistoryPurchased = itemView.findViewById(R.id.tv_history_purchased_at)
        }

        // Method buatan untuk mempermudah pengisian data per item RecyclerView. Jadi ketika pada bagian onBindViewHolder kita hanya perlu mengirimkan 1 objek History saja.
        fun setData(orders: Orders) {
            tvHistoryPurchased.text = StringBuilder("Purchased at : ${orders.purchasedAt}")
            tvHistoryTotal.text = StringBuilder("Total : ${orders.totalPrice}$")
        }
    }
}