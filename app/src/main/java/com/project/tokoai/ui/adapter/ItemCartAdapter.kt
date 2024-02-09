package com.project.tokoai.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase
import com.project.tokoai.R
import com.project.tokoai.model.Item

class ItemCartAdapter(private val userKey: String) :
    RecyclerView.Adapter<ItemCartAdapter.ViewHolder>() {
    /*
     Pendeklarasian variable instance dari Firebase Realtime Database serta menentukan path mana
     yang akan diolah
     */
    private val database = FirebaseDatabase.getInstance()
    private val databaseCartReference = database.getReference("cart/")

    // Variable dibawah ini digunakan untuk menampung list item.
    private val listItem: ArrayList<Item> = ArrayList()

    /*
    Method buatan ini digunakan untuk mengisi variable ArrayList Item diatas. Kosongkan terlebih
    dahulu variable nya baru tambahkan semua list dari parameter kedalam variable diatas.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setListItem(itemList: ArrayList<Item>) {
        listItem.clear()
        listItem.addAll(itemList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate layout per item dengan layout yang telah dibuat di direktori layout yaitu item_cart_row.
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Set Data per item dengan method buatan pada Class ViewHolder dibawah.
        holder.setData(listItem[position])
    }

    // Mengembalikan berapa jumlah item yang ditampilkan di RecyclerView. Jumlah akan fleksibel mengikuti size dari List Item.
    override fun getItemCount() = listItem.size

    /*
    Sebuah Class untuk menyambungkan dan/atau mengisi item-item view pada layout item_cart_row sebelumnya.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Pendeklarasian variable view yang ada pada item_cart_row
        private var tvItemName: TextView
        private var tvPrice: TextView
        private var tvQuantity: TextView
        private var btnMinus: MaterialButton
        private var btnPlus: MaterialButton
        private var btnDelete: ImageView

        init {
            // Ketika Class pertama kali dipanngin maka variable yang dideklarasika diatas akan langsung diinisialisasi dengan id yang sesuai pada layout item_cart_row
            tvItemName = itemView.findViewById(R.id.tv_ai_name)
            tvPrice = itemView.findViewById(R.id.tv_ai_price)
            tvQuantity = itemView.findViewById(R.id.tv_quantity)
            btnMinus = itemView.findViewById(R.id.btn_minus)
            btnPlus = itemView.findViewById(R.id.btn_plus)
            btnDelete = itemView.findViewById(R.id.btn_delete)
        }

        // Method buatan untuk mempermudah pengisian data per item RecyclerView. Jadi ketika pada bagian onBindViewHolder kita hanya perlu mengirimkan 1 objek item saja.
        fun setData(item: Item) {
            tvItemName.text = item.ai_name
            tvPrice.text = StringBuilder("Price : ${item.ai_price * item.qty}$")
            tvQuantity.text = "${item.qty}"

            btnMinus.setOnClickListener {
                item.qty--
                if (item.qty < 1) {
                    item.qty = 0
                    databaseCartReference.child(userKey).child("item").child(item.itemIndex)
                        .removeValue()
                }
                tvQuantity.text = StringBuilder("${item.qty}")

                val price = item.ai_price * item.qty

                tvPrice.text = StringBuilder("Price : $price$")

                if (item.qty != 0) {
                    databaseCartReference.child(userKey).child("item").child(item.itemIndex)
                        .child("qty").setValue(item.qty)
                }
            }

            btnPlus.setOnClickListener {
                item.qty++
                tvQuantity.text = StringBuilder("${item.qty}")

                val price = item.ai_price * item.qty

                tvPrice.text = StringBuilder("Price : $price$")

                databaseCartReference.child(userKey).child("item").child(item.itemIndex)
                    .child("qty").setValue(item.qty)
            }

            btnDelete.setOnClickListener {
                databaseCartReference.child(userKey).child("item").child(item.itemIndex)
                    .removeValue()
            }
        }
    }
}