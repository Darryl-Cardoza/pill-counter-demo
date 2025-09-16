package com.example.pillcountingnewmodels.core.room.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pill_count_txn_details",
    foreignKeys = [
        ForeignKey(
            entity = PillCountTxnEntity::class,
            parentColumns = ["txnId"],
            childColumns = ["txnId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("txnId")]
)
data class PillCountTxnDetailsEntity(
    @PrimaryKey(autoGenerate = true) val txnDetailsId: Long = 0,
    val txnId: Long,
    val txnDetailsNo: Int?,
    val pillCount: Int?,
    val imagePath: String?,
    val type: String?,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)