package com.example.pillcountingnewmodels.core.room.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.common.base.Equivalence
import org.intellij.lang.annotations.Language

import androidx.room.*


@Entity(
    tableName = "pill_count_txn",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = DrugMasterEntity::class,
            parentColumns = ["drugId"],
            childColumns = ["drugId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("userId"),
        Index("drugId")
    ]
)
data class PillCountTxnEntity(
    @PrimaryKey(autoGenerate = true) val txnId: Long = 0,

    val userId: Long?,
    val drugId: Long?,

    val countType: CountType,
    val targetCount: Int?,
    val status: CountStatus,

    val note: String?,
    val expiry: String?,
    val lotNo: String?,
    val barcodeImage: String?,
    val isSubstitute: Boolean = false,
    val rxNo: String?,
    val refillNo: String?,
    val patientName: String?,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
