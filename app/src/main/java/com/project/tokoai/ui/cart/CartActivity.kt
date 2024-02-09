package com.project.tokoai.ui.cart

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.*
import com.project.tokoai.R
import com.project.tokoai.model.Item
import com.project.tokoai.model.Orders
import com.project.tokoai.model.PackageItemView
import com.project.tokoai.ui.adapter.BundlingItemCartAdapter
import com.project.tokoai.ui.adapter.ItemCartAdapter
import com.project.tokoai.ui.adapter.ThanksAdapter
import com.project.tokoai.ui.main.MainActivity.Companion.USER_KEY
import java.text.SimpleDateFormat
import java.util.*

class CartActivity : AppCompatActivity() {
    // Pendefinisian variable global privat dalam satu Class
    private lateinit var linearItem: LinearLayout
    private lateinit var linearBundling: LinearLayout
    private lateinit var rvItemCart: RecyclerView
    private lateinit var rvBundlingCart: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnCheckout: MaterialButton

    // Variable untuk menerima kiriman userKey dari Intent
    private var userKey: String? = null

    /*
    List untuk menampung data Item dan Item Bundling yang telah
    ditambahkan pada Cart
     */
    private val cartItem: ArrayList<Item> = ArrayList()
    private val cartBundling: ArrayList<PackageItemView> = ArrayList()

    // Variable yang akan dimanfaatkan untuk menghitung total harga
    private var totalItemPrice: Double = 0.0
    private var totalBundlingPrice: Double = 0.0

    // Variable instance dari Firebase Realtime Database
    private val database = FirebaseDatabase.getInstance()

    /*
     Variable dimana dari instance sebelumnya lebih di-spesifikkan kita akan berurusan dengan
     struktur path mana, dimana sekarang kita spesifikkan pada path cart dan history.
     */
    private val databaseCartReference = database.getReference("cart/")
    private val databaseHistoryReference = database.getReference("history/")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // Inisialisasi variable View dengan menyambungkan id yang berada pada layout-nya.
        linearItem = findViewById(R.id.layout_item)
        linearBundling = findViewById(R.id.layout_bundling)
        rvItemCart = findViewById(R.id.rv_item_cart)
        rvBundlingCart = findViewById(R.id.rv_bundling_cart)
        progressBar = findViewById(R.id.progressbar)
        btnCheckout = findViewById(R.id.btn_checkout)

        userKey = intent.getStringExtra(USER_KEY)

        setCartData()
        setListeners()
    }

    // Method buatan untuk menampung logika variable yang dapat di-Klik.
    @SuppressLint("SimpleDateFormat")
    private fun setListeners() {

        // Logika ketika tombol Checkout di-Klik.
        btnCheckout.setOnClickListener {

            /*
            Jika total harga semua 0 maka tidak bisa checkout, jika sudah benar
            maka tambahkan data order ke history dengan child userKey sebagai
            penanda history dari akun yang bersangkutan.
            */
            if ((this.totalItemPrice + this.totalBundlingPrice) == 0.0) {
                showToast("Can't checkout, cart must have at least 1 item!")
            } else {
                val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                val currentDate = sdf.format(Date())

                val orders = Orders()
                orders.purchasedAt = currentDate
                orders.totalPrice = this.totalItemPrice + this.totalBundlingPrice

                databaseHistoryReference.child(userKey!!).push()
                    .setValue(orders) { error: DatabaseError?, _: DatabaseReference? ->
                        if (error != null) {
                            showToast("Failed to Checkout: ${error.message}")
                        } else {
                            /*
                            Jika data dari Cart berhasil di Checkout dan masuk History, maka
                            hapus data dari Cart lalu kembalikan lagi ke Main Activity
                            dan setResult agar data quantity dari Main Activity bisa
                            terupdate kembali menjadi 0.
                             */
                            showToast("Successfully Purchased!")
                            databaseCartReference.child(userKey!!).removeValue()
                            val intent = Intent(this, ThanksAdapter::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
            }
        }
    }

    // Method buatan untuk mengambil data item dari Realtime Database.
    private fun setCartData() {
        if (userKey != null) {

            /*
            Memasang Listener yang hanya akan terus berjalan untuk mendapatkan
            seluruh data item pada cart.

            Jika terjadi perubahan pada child maka onDataChange akan terpanggil dan
            data akan otomatis terupdate ke RecyclerView kembali, serta harga
            akan otomatis terkalkulasi ulang.
            */
            databaseCartReference.child(userKey!!).child("item")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        cartItem.clear()

                        if (snapshot.value == null) {
                            totalItemPrice = 0.0
                            calculateTotalPrice()
                            databaseCartReference.removeEventListener(this)
                        }

                        for (dataSnap in snapshot.children) {
                            val item: Item? = dataSnap.getValue(Item::class.java)
                            item?.itemIndex = dataSnap.key!!
                            cartItem.add(item!!)
                        }

                        if (cartItem.isEmpty()) {
                            linearItem.visibility = View.GONE
                        } else {
                            linearItem.visibility = View.VISIBLE
                            setItemRecycleView(cartItem)

                            totalItemPrice = 0.0

                            for (i in cartItem) {
                                totalItemPrice += (i.qty * i.ai_price)
                            }

                            calculateTotalPrice()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showToast("Error: ${error.message}")
                    }
                })

            /*
            Memasang Listener yang hanya akan terus berjalan untuk mendapatkan
            seluruh data bundling item pada cart.

            Jika terjadi perubahan pada child maka onDataChange akan terpanggil dan
            data akan otomatis terupdate ke RecyclerView kembali, serta harga
            akan otomatis terkalkulasi ulang.
            */
            databaseCartReference.child(userKey!!).child("bundling")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        cartBundling.clear()

                        if (snapshot.value == null) {
                            totalBundlingPrice = 0.0
                            calculateTotalPrice()
                            databaseCartReference.removeEventListener(this)
                        }

                        for (dataSnap in snapshot.children) {
                            val packageItemView: PackageItemView? =
                                dataSnap.getValue(PackageItemView::class.java)
                            packageItemView?.itemIndex = dataSnap.key!!
                            cartBundling.add(packageItemView!!)
                        }

                        if (cartBundling.isEmpty()) {
                            linearBundling.visibility = View.GONE
                        } else {
                            linearBundling.visibility = View.VISIBLE
                            setItemBundlingRecycleView(cartBundling)

                            totalBundlingPrice = 0.0

                            for (i in cartBundling) {
                                totalBundlingPrice += (i.qty * i.price)
                            }

                            calculateTotalPrice()
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {

                        showToast("Error: ${error.message}")
                    }
                })
        }
    }

    /*
    Method pembantu untuk membantu pengisian data item di RecyclerView item cart.
     */
    private fun setItemRecycleView(listCartItem: ArrayList<Item>) {
        val itemCartAdapter = ItemCartAdapter(userKey!!)
        itemCartAdapter.setListItem(listCartItem)

        rvItemCart.adapter = itemCartAdapter
        rvItemCart.layoutManager = LinearLayoutManager(this)
    }

    /*
    Method pembantu untuk membantu pengisian data bundling item di RecyclerView bundling item cart.
     */
    private fun setItemBundlingRecycleView(listCartItem: ArrayList<PackageItemView>) {
        val bundlingItemCartAdapter = BundlingItemCartAdapter(userKey!!)
        bundlingItemCartAdapter.setListBundlingItem(listCartItem)

        rvBundlingCart.adapter = bundlingItemCartAdapter
        rvBundlingCart.layoutManager = LinearLayoutManager(this)
    }

    /*
    Method pembantu untuk menghitung harga total yang dimana akan langsung ditampilkan
    teksnya di tombol Checkout.
     */
    private fun calculateTotalPrice() {
        val result = this.totalItemPrice + this.totalBundlingPrice
        btnCheckout.text = StringBuilder("Checkout ${result}$")
    }

    /*
    Override method onBackPressed agar ketika diklik tombol back data menjalankan method
    getItemData() yang akan mengupdate cart ke tampilan sebelumnya (Main)
     */
    override fun onBackPressed() {
        val intent = Intent()
        setResult(RESULT_OK, intent)
        finish()
    }

    /*
    Method buatan untuk membantu kita dalam memanggil Toast.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}