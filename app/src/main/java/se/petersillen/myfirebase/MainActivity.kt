package se.petersillen.myfirebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel

    var todoadapter = TodoAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        todoadapter.parentActivity = this
        todoRecview.layoutManager = LinearLayoutManager(this)
        todoRecview.adapter = todoadapter

        viewModel.errorMessage.observe(this, Observer { errText ->
            Toast.makeText(this, errText, Toast.LENGTH_LONG).show()
        })

        viewModel.loadingMessage.observe(this, Observer {
            if (it == true) {
                loadingCL.visibility = View.VISIBLE
            } else {
                loadingCL.visibility = View.INVISIBLE
            }
        })

        viewModel.startBitmap.observe(this, Observer { loadedBitmap ->
            todoStartImageView.setImageBitmap(loadedBitmap)
        })
        viewModel.loadImage()

        viewModel.todolist.observe(this, Observer {
            todoadapter.notifyDataSetChanged()
        })

        
        /*
        // Write a message to the database
        val database = Firebase.database
        val myRef = database.getReference("message")
        myRef.setValue("ABC")
        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue<String>()
                Log.d("BILLDEBUG", "Value is: $value")
            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("BILLDEBUG", "Failed to read value.", error.toException())
            }
        })
        */

        todoBtn.setOnClickListener {
            viewModel.addTodo(todoET.text.toString())
            todoET.setText("")
        }

        logOutBTN.setOnClickListener {
            viewModel.auth.signOut()
            val intent = Intent(this, LoginRegisterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        if(viewModel.auth.currentUser == null) {
            val intent = Intent(this, LoginRegisterActivity::class.java)
            startActivity(intent)
        } else {

            Log.i("DEBUG","USER ID IS " + viewModel.auth.currentUser!!.uid)

            viewModel.loadTodo()
        }
    }
}