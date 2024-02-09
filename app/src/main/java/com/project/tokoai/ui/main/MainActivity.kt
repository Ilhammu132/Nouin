package com.project.tokoai.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.project.tokoai.R
import com.project.tokoai.model.Item
import com.project.tokoai.model.PackageItem
import com.project.tokoai.model.PackageItemView
import com.project.tokoai.ui.adapter.ItemAdapter
import com.project.tokoai.ui.cart.CartActivity
import com.project.tokoai.ui.history.HistoryActivity

class MainActivity : AppCompatActivity() {
    // Pendefinisian variable global privat dalam satu Class
    private lateinit var rvItem: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnCart: FloatingActionButton
    private lateinit var linearBtn: LinearLayout
    private lateinit var btnItem: MaterialButton
    private lateinit var btnBundling: MaterialButton

    // Variable instance dari Firebase Realtime Database
    private val database = FirebaseDatabase.getInstance()

    /*
     Variable dimana dari instance sebelumnya lebih di-spesifikkan kita akan berurusan dengan
     struktur path mana, dimana sekarang kita spesifikkan pada path data dan cart.
     */
    private val databaseItemReference = database.getReference("data/item")
    private val databaseBundlingReference = database.getReference("data/package_item")
    private val databaseCartReference = database.getReference("cart/")

    // Variable ArrayList Item dan Item Bundling untuk menampung semua data AI kita.
    private val itemList: ArrayList<Item> = ArrayList()
    private val itemBundlingList: ArrayList<PackageItemView> = ArrayList()

    // Variable untuk menampung Adapter ShoeAdapter untuk RecyclerView kita.
    private val itemAdapter = ItemAdapter(this)

    // Variable untuk menerima kiriman userKey dari Intent
    private var userKey: String? = null

    /*
    Variable penanda yang dimanfaatkan untuk menandakan apakan tampilan kita Item
    atau Bundling Item.
     */
    private var isItem = true

    /*
    Variable pengganti startActivity() ketika ingin berpindah Activity dengan intent.
    variable ini dapat menerima masukan.

    Misal pada logika dibawah ketika kita klik tombol Cart kita menggunakan variable ini
    untuk berpindah ke Halaman Cart yang dimana jika result telah di-set dari halaman Cart
    maka dilakukan pengecekan result seperti yang bisa dilihat dibawah ketika RESULT_OK maka
    langsung panggil method getItemData() (Method untuk merefresh/mengambil ulang data dari
    firebase agar tampilan item dapat langsung ter-update)
     */
    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.data != null) {
            if (result.resultCode == RESULT_OK) {
                getItemData()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi variable View dengan menyambungkan id yang berada pada layout-nya.
        rvItem = findViewById(R.id.rv_item)
        progressBar = findViewById(R.id.progressbar)
        btnCart = findViewById(R.id.btn_cart)
        linearBtn = findViewById(R.id.layout_btn)
        btnItem = findViewById(R.id.btn_item)
        btnBundling = findViewById(R.id.btn_item_bundling)

        userKey = intent.getStringExtra(USER_KEY)

        if (isItem) {
            btnItem.isEnabled = false
        }

        getItemData()
        setListeners()
    }

    // Method buatan untuk mengambil data item dari Realtime Database.
    private fun getItemData() {
        showLoading(true)

        /*
        Memasang Listener yang hanya berjalan sekali setelah data didapatkan untuk mendapatkan
        seluruh data sepatu kita.
         */
        databaseItemReference.addListenerForSingleValueEvent(object : ValueEventListener {

            // dataSnapshot merupakan variable yang berisi semua data kita pada path /data/item.
            override fun onDataChange(snapshot: DataSnapshot) {
                // Kosongkan list dari itemList terlebih dahulu agar tidak saling tertumpuk
                itemList.clear()

                /*
                Perulangan untuk menangkap data satu persatu dari snapShot dan memasukkannya kedalam
                variable item secara temporary. yang nantinya setiap satu perulangan akan ditambahkan
                objek item tersebut kedalam list item kita.
                 */
                for (dataSnap in snapshot.children) {
                    val item: Item? = dataSnap.getValue(Item::class.java)
                    itemList.add(item!!)
                }


                /*
                Setelah menerima data item dan data tidak kosong, maka lanjut untuk mengambil data Bundling
                Item
                 */
                if (itemList.isNotEmpty()) {
                    databaseBundlingReference.addListenerForSingleValueEvent(object :
                        ValueEventListener {

                        // dataSnapshot merupakan variable yang berisi semua data kita pada path /data/item.
                        override fun onDataChange(snapshot: DataSnapshot) {

                            // Kosongkan list dari itemBundlingList terlebih dahulu agar tidak saling tertumpuk
                            itemBundlingList.clear()
                            val packageBundlingItem: ArrayList<PackageItem> = ArrayList()

                            /*
                            Perulangan untuk menangkap data satu persatu dari snapShot dan memasukkannya kedalam
                            variable packageViewItem secara temporary. yang nantinya setiap satu perulangan akan ditambahkan
                            objek informasi bundling item tersebut kedalam list packageBundling kita.
                             */
                            for (dataSnap in snapshot.children) {
                                val packageItem: PackageItem? =
                                    dataSnap.getValue(PackageItem::class.java)
                                packageBundlingItem.add(packageItem!!)
                            }

                            /*
                            Setelah data didapatkan dan data tidak kosong, maka olah data dari package bundling item
                            agar dapat dibentuk menjadi list data Bundling Item yang dapat kita tampilkan.

                            Karena isi dari package bundling item hanya berisi List Nomor AI dalam paket
                            dan diskon.
                             */
                            if (packageBundlingItem.isNotEmpty()) {

                                for (i in 0 until packageBundlingItem.size) {
                                    val packageItemTemp = PackageItemView()
                                    packageItemTemp.packageName = packageBundlingItem[i].packageName

                                    for (index in packageBundlingItem[i].items) {
                                        val itemTemp = itemList[index - 1].copy()
                                        itemTemp.ai_price *= (1.0 - packageBundlingItem[i].discount)
                                        packageItemTemp.items.add(itemTemp)
                                    }

                                    itemBundlingList.add(packageItemTemp)
                                }
                            }

                            /*
                            Setelah list Bundling Item selesai diolah, maka lanjut untuk cek
                            apakah user pernah memiliki item di keranjang agar quantity dapat
                            auto ter-set seperti yang pernah user inputkan sebelumnya.
                             */
                            cartObserver()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            showToast("Error : ${error.message}")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Error : ${error.message}")
            }
        })

    }

    // Method buatan untuk menampung logika variable yang dapat di-Klik.
    private fun setListeners() {

        /*
        Logika ketika tombol Item diklik maka set isItem menjadi True
        dan panggil method pembantu setRecyclerView() agar data tampilan
        recyclerView dapat berubah menjadi tampilan item.
         */
        btnItem.setOnClickListener {
            btnItem.isEnabled = false
            btnBundling.isEnabled = true
            isItem = true
            setRecyclerView()
        }

        /*
        Logika ketika tombol Bundling Item diklik maka set isItem menjadi false
        dan panggil method pembantu setRecyclerView() agar data tampilan
        recyclerView dapat berubah menjadi tampilan bundling item.
         */
        btnBundling.setOnClickListener {
            btnItem.isEnabled = true
            btnBundling.isEnabled = false
            isItem = false
            setRecyclerView()
        }

        // Logika ketika tombol Cart di-Klik.
        btnCart.setOnClickListener {

            // Ambil list Item dan ItemBundling dari adapter
            var cartItem = itemAdapter.getListItem()
            var cartBundlingItem = itemAdapter.getListBundlingItem()

            // Filter data tersebut, hilangkan yang quantitynya masih 0
            cartItem = cartItem.filter { it.qty > 0 } as ArrayList<Item>
            cartBundlingItem = cartBundlingItem.filter { it.qty > 0 } as ArrayList<PackageItemView>

            /*
             Jika kedua data Item dan Bundling Item tidak ada quantity nya maka tidak bisa dimasukkan
             ke keranjang.

             Selain itu add item ke Realtime Database dengan child cart/(item atau bundling) sesuai
             dengan data apa yang dimasukkan ke keranjang.
             */
            if (cartItem.isEmpty() && cartBundlingItem.isEmpty()) {
                showToast("You didn't add anything!")
            } else {
                if (userKey != null) {
                    databaseCartReference.child(userKey!!).child("item").setValue(
                        cartItem
                    ) { error: DatabaseError?, _: DatabaseReference? ->

                        if (error != null) {
                            showToast("Failed to add to Cart: ${error.message}")
                        } else {
                            databaseCartReference.child(userKey!!).child("bundling").setValue(
                                cartBundlingItem
                            ) { err: DatabaseError?, _: DatabaseReference? ->
                                if (err != null) {
                                    showToast("Failed to add to Cart: ${err.message}")
                                } else {
                                    showToast("Successfully Added to Cart!")
                                    val iCart = Intent(this, CartActivity::class.java)
                                    iCart.putExtra(USER_KEY, userKey)
                                    resultLauncher.launch(iCart)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /*
    Method buatan untuk mengambil data dari keranjang user dan mengupdate tampilan
    quantity dari item ataupun bundling item yang pernah dimasukkan ke keranjang.
     */
    private fun cartObserver() {
        databaseCartReference.child(userKey!!).child("item")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val cartItemTemp = ArrayList<Item>()

                    for (dataSnap in snapshot.children) {
                        val item: Item? = dataSnap.getValue(Item::class.java)
                        item?.itemIndex = dataSnap.key!!
                        cartItemTemp.add(item!!)
                    }

                    for (i in 0 until itemList.size) {
                        for (j in 0 until cartItemTemp.size) {
                            if (i.toString() == cartItemTemp[j].itemIndex) {
                                itemList[i].qty = cartItemTemp[j].qty
                            }
                        }
                    }

                    databaseCartReference.child(userKey!!).child("bundling")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                showLoading(false)
                                val cartBundlingTemp = ArrayList<PackageItemView>()

                                for (dataSnap in snapshot.children) {
                                    val packageItemView: PackageItemView? =
                                        dataSnap.getValue(PackageItemView::class.java)
                                    packageItemView?.itemIndex = dataSnap.key!!
                                    cartBundlingTemp.add(packageItemView!!)
                                }

                                for (i in 0 until itemBundlingList.size) {
                                    for (j in 0 until cartBundlingTemp.size) {
                                        if (i.toString() == cartBundlingTemp[j].itemIndex) {
                                            itemBundlingList[i].qty = cartBundlingTemp[j].qty
                                        }
                                    }
                                }

                                setRecyclerView()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                showToast("Error update Cart: ${error.message}")
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Error update Cart: ${error.message}")
                }
            })

    }

    // Method buatan untuk mempermudah konfigurasi RecyclerView
    private fun setRecyclerView() {
        if (isItem) {
            // Set data kedalam list pada Adapter RecyclerView menggunakan method buatan kita sebelumnya didalam ShoeAdapter
            itemAdapter.toggleViewMode(true)
            itemAdapter.setListItem(itemList)

            // Set adapter dan LayoutManager recyclerView agar data dapat tampil ke layar.
            rvItem.adapter = itemAdapter
            rvItem.layoutManager = LinearLayoutManager(this)
        } else {
            // Set data kedalam list pada Adapter RecyclerView menggunakan method buatan kita sebelumnya didalam ShoeAdapter
            itemAdapter.toggleViewMode(false)
            itemAdapter.setListBundlingItem(itemBundlingList)

            // Set adapter dan LayoutManager recyclerView agar data dapat tampil ke layar.
            rvItem.adapter = itemAdapter
            rvItem.layoutManager = LinearLayoutManager(this)
        }
    }

    /*
    Method buatan untuk memperlihatkan dan menyembunyikan RecyclerView dan progressBar
    saat data sedang diambil dari Realtime Database
    */
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            linearBtn.visibility = View.GONE
            rvItem.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            linearBtn.visibility = View.VISIBLE
            rvItem.visibility = View.VISIBLE
        }
    }

    /*
    Method buatan untuk membantu kita dalam memanggil Toast.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_history) {
            val iHistory = Intent(this, HistoryActivity::class.java)
            iHistory.putExtra(USER_KEY, userKey)
            startActivity(iHistory)
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val USER_KEY: String = "user_key"
    }
}