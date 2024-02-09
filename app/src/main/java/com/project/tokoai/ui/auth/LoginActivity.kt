package com.project.tokoai.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.tokoai.R
import com.project.tokoai.model.User
import com.project.tokoai.ui.main.MainActivity
import com.project.tokoai.ui.main.MainActivity.Companion.USER_KEY


class LoginActivity : AppCompatActivity() {
    // Pendefinisian variable global privat dalam satu Class
    private lateinit var edEmail: EditText
    private lateinit var edPassword: EditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var btnToRegister: TextView

    // Variable instance dari Firebase Realtime Database
    private val database = FirebaseDatabase.getInstance()

    /*
     Variable dimana dari instance sebelumnya lebih di-spesifikkan kita akan berurusan dengan
     struktur path mana, dimana sekarang kita spesifikkan pada path /user.
     */
    private val databaseReference = database.getReference("user")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi variable View dengan menyambungkan id yang berada pada layout-nya.
        edEmail = findViewById(R.id.ed_email)
        edPassword = findViewById(R.id.ed_password)
        btnLogin = findViewById(R.id.btn_login)
        progressBar = findViewById(R.id.progressbar)
        btnToRegister = findViewById(R.id.tv_to_register)

        setStatusBar()
        setListeners()
    }

    // Method buatan untuk mengubah warna statusBar menjadi putih
    private fun setStatusBar() {
        val window: Window = this.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
    }

    // Method buatan untuk menampung logika variable yang dapat di-Klik.
    private fun setListeners() {
        // Logika ketika tombol Login di-Klik.
        btnLogin.setOnClickListener {

            /*
            Pengecekan validasi kolom dilakukan sebelum menjalankan logika untuk Login dengan
            memanggil method isValid()
             */
            if (isValid()) {
                // Memunculkan progressBar.
                showLoading(true)

                // Menampung isi text dari setiap kolom kedalam variable.
                val email = edEmail.text.toString()
                val password = edPassword.text.toString()

                // Variable sebagai penanda
                var flag = false

                /*
                Pada penjelasan databaseReference sebelumnya dimana kita spesifikkan pada pengolahan
                data pada path /user. Disini kita memasang sebuah listener untuk mengambil semua
                data yang ada didalam path /user.
                 */
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

                    // dataSnapshot merupakan variable yang berisi semua data kita pada path /users.
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        /*
                        Dibawah ini perulangan untuk mengecek apakah terdapat user dengan email dan
                        password yang sama dengan apa yang diinputkan sebelumnya. Kita ambil satu
                        persatu item pada dataSnapshot dan lakukan pengecekan manual dengan if.

                        Ketika cocok maka set flag jadi true dan pindahkan user ke halaman Main.
                         */
                        for (dataSnap in dataSnapshot.children) {
                            val user: User? = dataSnap.getValue(User::class.java)
                            if (user?.email == email && user.password == password) {
                                showLoading(false)
                                flag = true
                                val userKey = dataSnap.key
                                val iMain = Intent(this@LoginActivity, MainActivity::class.java)
                                iMain.putExtra(USER_KEY, userKey)
                                startActivity(iMain)
                                finish()
                                break
                            }
                        }


                        /*
                        Dibawah ini dilakukan pengecekan flag. Jika masih false maka langsung
                        keluarkan toast informasi tidak bisa masuk.
                         */
                        if (!flag) {
                            showLoading(false)
                            showToast("Can't login!")
                        }
                    }

                    // Method yang akan terpanggil ketika listener ter-Cancel.
                    override fun onCancelled(databaseError: DatabaseError) {
                        showLoading(false)
                        showToast("Error: ${databaseError.message}")
                    }
                })
            }
        }

        // Logika untuk berpindah ke Register
        btnToRegister.setOnClickListener {
            val iRegister = Intent(this, RegisterActivity::class.java)
            startActivity(iRegister)
        }
    }

    /*
    Method pengecekan dimana ketika kolom email/password masih kosong/salah maka akan
    mengeluarkan Toast serta mengembalikan nilai false, dimana method ini dipanggil ketika
    mengeklik tombol Login.
    */
    private fun isValid(): Boolean {
        return if (!Patterns.EMAIL_ADDRESS.matcher(edEmail.text.toString()).matches()) {
            showToast("Email format is worng!")
            false
        } else if (edEmail.text.isEmpty()) {
            showToast("Email field can't be blank!")
            false
        } else if (edPassword.text.isEmpty()) {
            showToast("Password field can't be blank!")
            false
        } else {
            true
        }
    }

    /*
    Method buatan untuk memperlihatkan dan menyembunyikan tombol Login dan progressBar,
    dimana method ini akan dipanggil ketika
    */
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            btnLogin.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            btnLogin.visibility = View.VISIBLE
        }
    }

    /*
    Method buatan untuk membantu kita dalam memanggil Toast.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}