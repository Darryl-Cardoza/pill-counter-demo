package com.example.pillcountingnewmodels.core.room.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.common.base.Equivalence
import org.intellij.lang.annotations.Language

@Entity(
    tableName = "drug_master",
    indices = [Index("ndc", unique = true)]
)
data class DrugMasterEntity(
    @PrimaryKey(autoGenerate = true) val drugId: Long = 0,
    val drugName: String?,
    val ndc: String?,
    val equivalence: String?,
    val drugType: String?,
    val createdAt: Long = System.currentTimeMillis()
)