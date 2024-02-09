package com.project.tokoai.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
Penggunaan data class dengan Parcelable untuk memudahkan proses pengiriman data, get maupun
set data pada class model.

Class ini digunakan hanya sebagai model untuk menampung data yang diolah ke Realtime
Database dan Class Activity maupun Adapter yang bersangkutan.
 */
@Parcelize
data class PackageItemView(
    var itemIndex: String = "",
    var packageName: String = "",
    var items: ArrayList<Item> = ArrayList(),
    var price: Double = 0.0,
    var qty: Int = 0
) : Parcelable