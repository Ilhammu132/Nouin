package com.project.tokoai.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
Penggunaan data class dengan Parcelable untuk memudahkan proses pengiriman data, get maupun
set data pada class model.

Class ini digunakan hanya sebagai model untuk menampung data yang diolah ke Realtime
Database.
 */
@Parcelize
data class PackageItem(
    var packageName: String = "",
    var discount: Double = 0.0,
    var items: List<Int> = listOf()
) : Parcelable
