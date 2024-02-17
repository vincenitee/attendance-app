package com.example.attendance

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.attendance.databinding.ActivityHomepageBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.Instant


class Homepage : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private lateinit var homePageBinding: ActivityHomepageBinding
    private val db = FirebaseFirestore.getInstance()
    private val studentCollectionRef = db.collection("student")
    private val eventCollectionRef = db.collection("event")

    private val codeScannerView by lazy { homePageBinding.scannerView }
    private val resultTextView by lazy { homePageBinding.scanIdNum }
    private val btnTimeOut by lazy { homePageBinding.btnTimeOut}
    private val btnTimeIn by lazy { homePageBinding.btnTimeIn }

    private val CAMERA_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homePageBinding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(homePageBinding.root)

        // requests permission
        setUpPermission()

        // starts scanner
        initializedCodeScanner()

    }

    private fun initializedCodeScanner(){
        // initializes scanner
        codeScanner = CodeScanner(this, codeScannerView)

        // configures scanner
        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = true

            // decode success
            decodeCallback  = DecodeCallback {
                runOnUiThread{
                    val idNumber = extractIdNumber(it)
                    if(idNumber != null){
                        getStudentInfo(idNumber)
                    }
                }
            }

            // camera initialization failure
            errorCallback = ErrorCallback {
                runOnUiThread {
                    showMessage(it.message)
                }
            }
        }
    }

    // retrieves event information
    private fun getEventInfo(eventID: String) = CoroutineScope(Dispatchers.IO).launch{

    }

    // retrieves student info from database
    private fun getStudentInfo(idNumber: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val documentSnapshot = withContext(Dispatchers.IO) {
                studentCollectionRef.document(idNumber).get().await()
            }

            val data = documentSnapshot.data?.get("first_name")
            withContext(Dispatchers.Main) {
                resultTextView.text = data?.toString() ?: "Student Not Found"
            }
        } catch (e: Exception) {
            Log.d("MainActivity", e.message ?: "")
        }
    }

    // filters id number
    private fun extractIdNumber(it: Result): String? {
        val resultText = it.text
        val pattern = Regex("[0-9]+")
        val matchedResult = pattern.findAll(resultText)
            .map { it.value }
        val extractedId = matchedResult.joinToString("")

        if(extractedId.length == 8){
            return extractedId
        }

        return null
    }

    private fun showMessage(message: String?) {
        Snackbar.make(homePageBinding.root,
            message?:"", Snackbar.LENGTH_LONG).show()
    }

    override fun onResume(){
        super.onResume()
        codeScanner.startPreview()
    }

    // throw resources when app is closed
    override fun onPause(){
        super.onPause()
        codeScanner.releaseResources()
    }

    // line 62- 84 setting up permission for API 23 above
    private fun setUpPermission(){
        val permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED){
            makeRequest()
        }
    }

    private fun makeRequest(){
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    showMessage("CAMERA PERMISSION REQUIRED")
                }
            }
        }
    }

    fun timeIn(view: View) {

    }
    fun timeOut(view: View) {
        //codes here
    }

}
