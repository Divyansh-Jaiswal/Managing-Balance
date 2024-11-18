package com.example.managingbalance

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var deletedTransaction: Transaction

    private lateinit var transactions: List<Transaction>
    private lateinit var oldTransactions : List<Transaction>

    private lateinit var transactionAdapter: Adapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerview: RecyclerView
    private lateinit var BUDGET:TextView
    private lateinit var spent:TextView
    private lateinit var balance: TextView
    private lateinit var db:AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        BUDGET=findViewById(R.id.budget)
        spent=findViewById(R.id.spends)
        balance=findViewById(R.id.balance)

        recyclerview = findViewById(R.id.transaction)
        var xDelta = 0f
        var yDelta = 0f
        val fab = findViewById<FloatingActionButton>(R.id.addBtn)


        fab.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    xDelta = view.x - event.rawX
                    yDelta = view.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    view.animate()
                        .x(event.rawX + xDelta)
                        .y(event.rawY + yDelta)
                        .setDuration(0)
                        .start()
                }
                MotionEvent.ACTION_UP -> {
                    // Perform a click if needed
                    view.performClick()
                }
            }
            true
        }

        fab.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }





        transactions= arrayListOf()

        transactionAdapter = Adapter(transactions)
        linearLayoutManager= LinearLayoutManager(this)

        db= Room.databaseBuilder(this,AppDatabase::class.java,"transactions").build()

        recyclerview.apply{
            adapter=transactionAdapter
            layoutManager=linearLayoutManager
        }

        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTransaction(transactions[viewHolder.adapterPosition])
            }

        }

        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(recyclerview)

    }
    private fun fetchAll()
    {
        GlobalScope.launch {
            transactions=db.transactionDao().getAll()
            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transactions)
            }
        }
    }
    private fun updateDashboard()
    {
        val totalAmount= transactions.map { it.amount }.sum()
        val budgetAmount=transactions.filter { it.amount >=0}.map {it.amount}.sum()
        val expense= totalAmount-budgetAmount

        balance.text= "%.2f".format(totalAmount)
        BUDGET.text= "%.2f".format(budgetAmount)
        spent.text= "%.2f".format(expense)
    }

    private fun undoDelete(){
        GlobalScope.launch {
            db.transactionDao().insertAll(deletedTransaction)

            transactions = oldTransactions

            runOnUiThread {
                transactionAdapter.setData(transactions)
                updateDashboard()
            }
        }
    }

    private fun showSnackbar(){
        val view = findViewById<View>(R.id.coordinator)
        val snackbar = Snackbar.make(view, "Transaction deleted!",Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this, R.color.red))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }

    private fun deleteTransaction(transaction: Transaction){
        deletedTransaction = transaction
        oldTransactions = transactions

        GlobalScope.launch {
            db.transactionDao().delete(transaction)

            transactions = transactions.filter { it.id != transaction.id }
            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transactions)
                showSnackbar()
            }
        }
    }



    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}