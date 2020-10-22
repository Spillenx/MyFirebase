package se.petersillen.myfirebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

@IgnoreExtraProperties
data class Todothing(
    var fbkey: String? = null,
    var todotitle: String? = "",
    var done: Boolean? = false
)

class MainViewModel: ViewModel() {

    var database: DatabaseReference = Firebase.database.reference
    var auth: FirebaseAuth = Firebase.auth

    var todolist = MutableLiveData<List<Todothing>>()
    val startBitmap = MutableLiveData<Bitmap>()

    val errorMessage = MutableLiveData<String>()
    val loadingMessage = MutableLiveData<Boolean>()

    fun loadImage() {
        var storage = Firebase.storage
        var storageRef = storage.reference

        var theStorageImageRef = storageRef.child("frog.jpg")

        val ONE_MEGABYTE: Long = 1024 * 1024
        loadingMessage.value = true
        theStorageImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {theBytes ->
            // Data for "images/island.jpg" is returned, use this as needed
            var theBitmap = BitmapFactory.decodeByteArray(theBytes, 0, theBytes.size)
            startBitmap.value = theBitmap
            loadingMessage.value = false
        }.addOnFailureListener {
            // Handle any errors
            errorMessage.value = "Kunde inte ladda bild!"
            loadingMessage.value = false
        }
    }

    fun loadTodo() {

        val todoListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI

                var tempTodolist = mutableListOf<Todothing>()
                for (todochild in dataSnapshot.children) {
                    val todo = todochild.getValue<Todothing>()
                    todo!!.fbkey = todochild.key
                    tempTodolist.add(todo!!)

                    //Log.i("DEBUG", todochild.key)
                    //Log.i("DEBUG", todo!!.todotitle)
                }
                todolist.value = tempTodolist
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("DEBUG", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        database.child("todoUsers").child(auth.currentUser!!.uid).orderByChild("done").addListenerForSingleValueEvent(todoListener)
    }

    fun addTodo(title: String) {
        var thingtodo = Todothing("", title, false)

        database.child("todoUsers").child(auth.currentUser!!.uid).push().setValue(thingtodo)

        loadTodo()
    }

    fun changeDone(todoNumber: Int) {
        if(todolist.value!![todoNumber].done == true) {

            database.child("todoUsers").child(auth.currentUser!!.uid).child(todolist.value!![todoNumber].fbkey!!).child("done").setValue(false)
            //database.child("todoUsers").child(auth.currentUser!!.uid).child(todolist[position].fbkey!!).removeValue()
        } else {
            database.child("todoUsers").child(auth.currentUser!!.uid).child(todolist.value!![todoNumber].fbkey!!).child("done").setValue(true)
        }

        loadTodo()
    }
}