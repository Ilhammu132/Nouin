package com.project.tokoai.ui.history

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.tokoai.R
import com.project.tokoai.model.Orders
import com.project.tokoai.ui.adapter.HistoryAdapter
import com.project.tokoai.ui.main.MainActivity.Companion.USER_KEY

class HistoryActivity : AppCompatActivity() {
    private lateinit var rvHistory: RecyclerView

    // Variable instance dari Firebase Realtime Database
    private val database = FirebaseDatabase.getInstance()

    /*
     Variable dimana dari instance sebelumnya lebih di-spesifikkan kita akan berurusan dengan
     struktur path mana, dimana sekarang kita spesifikkan pada path history.
     */
    private val databaseHistoryReference = database.getReference("history/")

    /*
    List untuk menampung data History yang telah
    ditambahkan pada History
     */
    private val historyList: ArrayList<Orders> = ArrayList()

    // Variable untuk menerima kiriman userKey dari Intent
    private var userKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Inisialisasi variable View dengan menyambungkan id yang berada pada layout-nya.
        rvHistory = findViewById(R.id.rv_history)

        userKey = intent.getStringExtra(USER_KEY)

        getHistoryData()
    }

    // Method buatan untuk mengambil data history dari Realtime Database.
    private fun getHistoryData() {
        if (userKey != null) {
            databaseHistoryReference.child(userKey!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        historyList.clear()

                        for (dataSnap in snapshot.children) {
                            val orders: Orders? =
                                dataSnap.getValue(Orders::class.java)
                            historyList.add(orders!!)
                        }

                        if (historyList.isNotEmpty()) {
                            val historyAdapter = HistoryAdapter()
                            historyAdapter.setListHistory(historyList)

                            rvHistory.adapter = historyAdapter
                            rvHistory.layoutManager = LinearLayoutManager(this@HistoryActivity)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showToast("Error : ${error.message}")
                    }

                })
        }
    }

    /*
    Method buatan untuk membantu kita dalam memanggil Toast.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}