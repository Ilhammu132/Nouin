package com.project.tokoai.ui.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.project.tokoai.R
import com.project.tokoai.model.Item
import com.project.tokoai.model.PackageItemView

class ItemAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /*
    Dibawah ini kita mendeklarasika 2 ViewHolder agar RecyclerView dapat menampilkan 2 tampilan
    antara Item dan Item Bundling Package
     */
    private lateinit var itemViewHolder: ItemViewHolder
    private lateinit var bundlingItemViewHolder: BundlingItemViewHolder

    // Variable dibawah ini digunakan untuk menampung list item.
    private val listItem: ArrayList<Item> = ArrayList()

    // Variable dibawah ini digunakan untuk menampung list item bundling.
    private val listBundlingItem: ArrayList<PackageItemView> = ArrayList()

    // Variable penanda agar kita bisa mengubah tampilan antara tampilan Item dengan Item Bundling
    private var isSwitched = true

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

    /*
    Method buatan ini digunakan untuk mengisi variable ArrayList Item Bundling diatas. Kosongkan terlebih
    dahulu variable nya baru tambahkan semua list dari parameter kedalam variable diatas.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setListBundlingItem(itemList: ArrayList<PackageItemView>) {
        listBundlingItem.clear()
        listBundlingItem.addAll(itemList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Inflate layout item dengan layout yang telah dibuat di direktori layout yaitu item_row_list.
        itemViewHolder = ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row_list, parent, false)
        )

        // Inflate layout item dengan layout yang telah dibuat di direktori layout yaitu item_bundling_row_list.
        bundlingItemViewHolder = BundlingItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bundling_row_list, parent, false)
        )

        /*
        Jika viewType ter-set menjadi mode item maka tampilkan layout item.
        Jika tidak (ter-set menjadi mode bundling) maka tampilkan layout bundling item.
         */
        return if (viewType == ITEM_MODE) {
            itemViewHolder
        } else {
            bundlingItemViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        /*
        Jika variable isSwitched true (tampilan merupakan layout item) maka setData item ke Class ViewHolder milik Item.
        Jika variable isSwitched false (tampilan merupakan layout bundling item) maka setData bundling item ke Class ViewHolder milik Bundling Item.
         */
        if (isSwitched) {
            itemViewHolder.setData(listItem[position], position)
        } else {
            bundlingItemViewHolder.setData(listBundlingItem[position], position)
        }
    }

    /*
    Jika variable isSwitched true (tampilan merupakan layout item) maka buat jumlah item sesuai dengan jumlah list data item.
    Jika variable isSwitched false (tampilan merupakan layout bundling item) maka buat jumlah item sesuai dengan jumlah list data bundling item.
    */
    override fun getItemCount(): Int {
        return if (isSwitched) {
            listItem.size
        } else {
            listBundlingItem.size
        }
    }

    /*
    Method buatan yang digunakan di MainActivity ketika tombol Item/Bundling Item diklik
    untuk mengubah variable isSwitched (untuk mengubah tampilan antara item-bundling item_
     */
    fun toggleViewMode(isItem: Boolean) {
        isSwitched = isItem
    }


    /*
    Sebuah Class untuk menyambungkan dan/atau mengisi item-item view pada layout item_row_list sebelumnya.
     */
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Pendeklarasian variable view yang ada pada item_row_list
        private var tvItemName: TextView
        private var tvItemCharacteristic: TextView
        private var tvPrice: TextView
        private var tvQuantity: TextView
        private var btnMinus: MaterialButton
        private var btnPlus: MaterialButton

        init {
            // Ketika Class pertama kali dipanngin maka variable yang dideklarasika diatas akan langsung diinisialisasi dengan id yang sesuai pada layout item_row_list
            tvItemName = itemView.findViewById(R.id.tv_ai_name)
            tvItemCharacteristic = itemView.findViewById(R.id.tv_ai_characteristic)
            tvPrice = itemView.findViewById(R.id.tv_ai_price)
            tvQuantity = itemView.findViewById(R.id.tv_quantity)
            btnMinus = itemView.findViewById(R.id.btn_minus)
            btnPlus = itemView.findViewById(R.id.btn_plus)
        }

        // Method buatan untuk mempermudah pengisian data per item RecyclerView. Jadi ketika pada bagian onBindViewHolder kita hanya perlu mengirimkan 1 objek item saja.
        fun setData(item: Item, position: Int) {
            tvItemName.text = item.ai_name
            tvItemCharacteristic.text = item.ai_characteristic
            tvPrice.text = StringBuilder("Price : ${item.ai_price}$")
            tvQuantity.text = StringBuilder("${item.qty}")

            btnMinus.setOnClickListener {
                item.qty--
                if (item.qty < 0) {
                    item.qty = 0
                }
                tvQuantity.text = StringBuilder("${item.qty}")

                listItem[position].qty = item.qty
            }

            btnPlus.setOnClickListener {
                item.qty++
                tvQuantity.text = StringBuilder("${item.qty}")

                listItem[position].qty = item.qty
            }
        }
    }

    /*
    Sebuah Class untuk menyambungkan dan/atau mengisi item-item view pada layout item_bundling_row_list sebelumnya.
     */
    inner class BundlingItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Pendeklarasian variable view yang ada pada item_shoe
        private var tvPackageName: TextView
        private var lvItem: ListView
        private var tvPrice: TextView
        private var tvQuantity: TextView
        private var btnMinus: MaterialButton
        private var btnPlus: MaterialButton

        init {
            // Ketika Class pertama kali dipanngin maka variable yang dideklarasika diatas akan langsung diinisialisasi dengan id yang sesuai pada layout item_bundling_row_list
            tvPackageName = itemView.findViewById(R.id.tv_package_name)
            lvItem = itemView.findViewById(R.id.tv_list_package)
            tvPrice = itemView.findViewById(R.id.tv_ai_price)
            tvQuantity = itemView.findViewById(R.id.tv_quantity)
            btnMinus = itemView.findViewById(R.id.btn_minus)
            btnPlus = itemView.findViewById(R.id.btn_plus)
        }

        // Method buatan untuk mempermudah pengisian data per item RecyclerView. Jadi ketika pada bagian onBindViewHolder kita hanya perlu mengirimkan 1 objek bundling item saja.
        fun setData(itemPackage: PackageItemView, position: Int) {
            tvPackageName.text = itemPackage.packageName

            val listAiName = ArrayList<String>()
            val listAiPrice = ArrayList<Double>()
            var totalPrice = 0.0

            for (item in itemPackage.items) {
                listAiName.add(item.ai_name)
                listAiPrice.add(item.ai_price)
                totalPrice += item.ai_price
            }

            listBundlingItem[position].price = totalPrice

            val itemNamePackageAdapter = ItemNamePackageAdapter(context, listAiName, listAiPrice)
            lvItem.adapter = itemNamePackageAdapter

            lvItem.post(Runnable {
                val listAdapter: ListAdapter = lvItem.adapter ?: return@Runnable
                var totalHeight: Int = lvItem.paddingTop + lvItem.paddingBottom
                val listWidth: Int = lvItem.measuredWidth
                for (i in 0 until listAdapter.count) {
                    val listItem: View = listAdapter.getView(i, null, lvItem)
                    listItem.measure(
                        View.MeasureSpec.makeMeasureSpec(listWidth, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    totalHeight += listItem.measuredHeight
                    Log.d("listItemHeight " + listItem.measuredHeight, "********")
                }
                Log.d("totalHeight $totalHeight", "********")
                val params: ViewGroup.LayoutParams = lvItem.layoutParams
                params.height =
                    totalHeight + lvItem.dividerHeight * (listAdapter.count - 1)
                lvItem.layoutParams = params
                lvItem.requestLayout()
            })

            tvPrice.text = StringBuilder("Price : ${totalPrice}$")

            tvQuantity.text = StringBuilder("${itemPackage.qty}")

            btnMinus.setOnClickListener {
                itemPackage.qty--
                if (itemPackage.qty < 0) {
                    itemPackage.qty = 0
                }
                tvQuantity.text = StringBuilder("${itemPackage.qty}")

                listBundlingItem[position].qty = itemPackage.qty
            }

            btnPlus.setOnClickListener {
                itemPackage.qty++
                tvQuantity.text = StringBuilder("${itemPackage.qty}")

                listBundlingItem[position].qty = itemPackage.qty
            }
        }
    }

    /*
    Method buatan untuk mengambil data list item yang akan dipanggil oleh MainActivity
    ketika tombol Cart diklik
     */
    fun getListItem(): ArrayList<Item> {
        return this.listItem
    }

    /*
    Method buatan untuk mengambil data list bundling item yang akan dipanggil oleh MainActivity
    ketika tombol Cart diklik
     */
    fun getListBundlingItem(): ArrayList<PackageItemView> {
        return this.listBundlingItem
    }

    /*
    Method yang dimanfaatkan untuk mengganti viewType antara Mode Item dan Mode Bundling berdasarkan
    isi dari variable isSwitched
     */
    override fun getItemViewType(position: Int): Int {
        return if (isSwitched) {
            ITEM_MODE
        } else {
            BUNDLING_MODE
        }
    }

    companion object {
        const val ITEM_MODE = 0
        const val BUNDLING_MODE = 1
    }
}