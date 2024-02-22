package com.example.attendance

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.example.attendance.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val studentCollectionRef = db.collection("student")

    private lateinit var binding: ActivityMainBinding
    private val idNumber by lazy { binding.IdNum }
    private val password by lazy { binding.password }
    private val btnLogin by lazy { binding.loginButton }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btnLogin.setOnClickListener {
//            showMessageAndNavigate("Authentication Success", Homepage::class.java)
            val idNumberValue = idNumber.text.toString()
            val pass = password.text.toString()

            when{
                idNumberValue.isEmpty() || pass.isEmpty() -> showMessage("ID Number and Password should not be empty")
                idNumberValue.length != 8 || !idNumberValue.isDigitsOnly() -> showMessage("Invalid ID Number")
                else -> signIn(idNumberValue, pass)
            }
        }
    }

    private fun signIn(idNumber: String, enteredPassword: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val documentSnapshot = studentCollectionRef.document(idNumber).get().await()
            val userRoleString = documentSnapshot.getString("role") ?: "STUDENT"
            val userRole = UserRole.valueOf(userRoleString.uppercase())
            val userData = UserData(userRole, documentSnapshot.getString("password") ?: "")

            if(authenticateUser(userData, enteredPassword)){
                withContext(Dispatchers.Main) {
                    showMessageAndNavigate("Authentication Success", Homepage::class.java)
                }
            } else{
                withContext(Dispatchers.Main){
                    showMessage("Authentication Failed")
                }
            }
        }catch (e: Exception){
            showMessage(e.message)
        }
    }

    private fun authenticateUser(userData: UserData, enteredPassword: String): Boolean{
        return userData.role == UserRole.OFFICER && userData.pass == enteredPassword
    }

    private inline fun <reified T : Activity> nextActivity(context: Context, clazz: Class<T>) {
        val intent = Intent(context, clazz)
        context.startActivity(intent)
    }

    private fun showMessage(message: String?) {
        Snackbar.make(binding.root, message?:"", Snackbar.LENGTH_LONG).show()
    }

    private inline fun <reified T : Activity> showMessageAndNavigate(message: String, clazz: Class<T>) {
        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
        nextActivity(this@MainActivity, clazz)
    }

}