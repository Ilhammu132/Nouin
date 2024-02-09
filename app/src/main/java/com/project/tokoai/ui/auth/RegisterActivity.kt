package com.project.tokoai.ui.auth

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
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.project.tokoai.R
import com.project.tokoai.model.User

class RegisterActivity : AppCompatActivity() {
    // Pendefinisian variable global privat dalam satu Class
    private lateinit var edEmail: EditText
    private lateinit var edPassword: EditText
    private lateinit var edConfirmPassword: EditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var btnToLogin: TextView

    // Variable instance dari Firebase Realtime Database
    private val database = FirebaseDatabase.getInstance()

    /*
     Variable dimana dari instance sebelumnya lebih di-spesifikkan kita akan berurusan dengan
     struktur path mana, dimana sekarang kita spesifikkan pada path /user.
     */
    private val databaseReference = database.getReference("user")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inisialisasi variable View dengan menyambungkan id yang berada pada layout-nya.
        edEmail = findViewById(R.id.ed_email)
        edPassword = findViewById(R.id.ed_password)
        edConfirmPassword = findViewById(R.id.ed_confirmation_password)
        btnRegister = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.progressbar)
        btnToLogin = findViewById(R.id.tv_to_login)

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

        // Logika ketika tombol Register di-Klik.
        btnRegister.setOnClickListener {

            /*
            Pengecekan validasi kolom dilakukan sebelum menjalankan logika untuk Register dengan
            memanggil method isValid()
             */
            if (isValid()) {
                // Memunculkan progressBar.
                showLoading(true)

                // Menampung isi text dari setiap kolom kedalam variable.
                val email = edEmail.text.toString()
                val password = edPassword.text.toString()

                // Membuat instance dari Class User untuk menampung data user yang nantinya akan dipush ke Realtime Database
                val user = User(email, password)

                /*
                Tambahkan secara otomatis data user dengan method push() maka nama child
                dari setiap akun akan auto digenerate menjadi sebuah key string acak.
                 */
                databaseReference.push().setValue(
                    user
                ) { error: DatabaseError?, _: DatabaseReference? ->
                    // Jika error tidak null maka keluarkan informasi error tersebut, jika tidak keluarkan informasi bahwa akun berhasil dibuat dan kembalikan ke Login.
                    if (error != null) {
                        showLoading(false)
                        showToast("Error: ${error.message}")
                    } else {
                        showLoading(false)
                        showToast("Account successfully registered! Please Login")
                        finish()
                    }
                }

            }
        }

        // Logika untuk berpindah ke Login kembali
        btnToLogin.setOnClickListener {
            finish()
        }
    }

    /*
    Method pengecekan dimana ketika kolom email/password masih kosong/salah maka akan
    mengeluarkan Toast serta mengembalikan nilai false, dimana method ini dipanggil ketika
    mengeklik tombol Register.
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
        } else if (edConfirmPassword.text.isEmpty()) {
            showToast("Confirmation Password field can't be blank!")
            false
        } else if (edPassword.text.toString() != edConfirmPassword.text.toString()) {
            showToast("Password and Confirmation Password must be same!")
            false
        } else {
            true
        }
    }

    /*
    Method buatan untuk memperlihatkan dan menyembunyikan tombol Login dan progressBar,
    dimana method ini akan dipanggil setelah tombol Register berhasil diklik.
    */
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            btnRegister.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            btnRegister.visibility = View.VISIBLE
        }
    }

    /*
    Method buatan untuk membantu kita dalam memanggil Toast.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}