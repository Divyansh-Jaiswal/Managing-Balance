package com.example.managingbalance

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.log

class AddTransactionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_transaction)
        val addTransactionBtn: Button= findViewById(R.id.addTransactionBtn)
        val labelInput :TextInputEditText= findViewById(R.id.labelInput)
        val amountInput :TextInputEditText= findViewById(R.id.amountInput)
        val descriptionInput :TextInputEditText= findViewById(R.id.descriptionInput)
        val labelLayout:TextInputLayout = findViewById(R.id.labelLayout)
        val amountLayout:TextInputLayout = findViewById(R.id.amountLayout)
        val descriptionLayout:TextInputLayout = findViewById(R.id.descriptionLayout)
        val closeBtn: ImageButton= findViewById(R.id.closeBtn)

        labelInput.addTextChangedListener {
            if(it!!.count()>0)
            {
                labelLayout.error=null
            }
            if (it!!.count()>0)
            {
                amountLayout.error=null
            }

        }

        addTransactionBtn.setOnClickListener{
            val l= labelInput.text.toString()
            val a= amountInput.text.toString().toDoubleOrNull()
            val d= descriptionInput.text.toString()


            if (l.isEmpty()) {
                labelLayout.error=  "Please enter a valid label"
            }
            else if (a==null) {
                amountLayout.error=  "Please enter a valid amount"
            }
            else
            {
                val transcation = Transaction(0,l,a,d)
                insert(transcation)
            }
        }
        closeBtn.setOnClickListener {
            finish()
        }
    }
    private fun insert(transaction: Transaction)
    {
        val db= Room.databaseBuilder(this,AppDatabase::class.java,"transactions").build()
        GlobalScope.launch {
            db.transactionDao().insertAll(transaction)
            finish()
        }
    }
}