package com.example.managingbalance

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TransactionDao {
    @Query("Select *from transactions")
    fun getAll():List<Transaction>

    @Insert
    fun insertAll(vararg transaction: Transaction)

    @Delete
    fun delete(transaction: Transaction)

    @Update
    fun update(vararg transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE amount>0")

    fun getPositiveAmount(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE amount<0")
    fun getNegativeAmount(): List<Transaction>
}