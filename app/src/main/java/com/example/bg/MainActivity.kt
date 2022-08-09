package com.example.bg

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.loader.content.CursorLoader
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.MediaType;
import okhttp3.MultipartBody.Part.Companion.create
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


class MainActivity : AppCompatActivity() {
    private lateinit var image: ImageView
    private lateinit var button: Button
    private val LOAD_IMAGE_RESULTS: Int = 1

    var backupFile: File? = null
    lateinit var apiInterface: MyInterface
    val TAG = "rawat"

    var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.btnPick);
        image = findViewById(R.id.imgView);

        //
        requestPermission()
        button.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK);
            i.type = "image/*";

            startActivityForResult(i, LOAD_IMAGE_RESULTS);
        }

        apiInterface = ApiClient.getClient()!!.create(MyInterface::class.java)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var ImageStream: InputStream? = null
        if (requestCode == LOAD_IMAGE_RESULTS && resultCode == RESULT_OK && data != null) {
            try {
                //Let's read the picked image -its URI
                val pickedImage: Uri? = data.data

                imageUri = data.data

                //Let's read the image path using content resolver
                ImageStream = contentResolver.openInputStream(pickedImage!!)

                //Now let's set the GUI ImageView data with data read from the picked file
                val selectedImage = BitmapFactory.decodeStream(ImageStream)
                image.setImageBitmap(selectedImage)
                backupFile = File(pickedImage.path)
                val absolutePath = backupFile!!.absolutePath

                Log.d(TAG, "onActivityResult: ${absolutePath}")

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } finally {
                if (ImageStream != null) {
                    try {
                        ImageStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

//    fun pickImage(view: View) {}

    fun hitapi(myFile: File) {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = File(
            Environment.getExternalStorageDirectory().toString(),
            "whatever_directory_existing_or_not/sub_dir_if_needed/"
        )
        storageDir.mkdirs() // make sure you call mkdirs() and not mkdir()
//        val image = File.createTempFile(
//            imageFileName,  // prefix
//            ".jpg",  // suffix
//            storageDir // directory
//        )
        val image = getRealPathFromURI(imageUri!!);

        // Save a file: path for use with ACTION_VIEW intents


        // Save a file: path for use with ACTION_VIEW intents
//        var mCurrentPhotoPath = "file:" + image.absolutePath
        Log.e("our file", image.toString())
//        return image

        val requserId: RequestBody =
            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), myFile)
        var reqImg: MultipartBody.Part? = null
        val reqFile: RequestBody =
            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), image!!)
        reqImg = MultipartBody.Part.createFormData("image", image, reqFile)

        val call = apiInterface.uploadUserImg(reqImg, 2)
        call.enqueue(object : Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>?, response: Response<UploadResponse>?) {
                Toast.makeText(
                    applicationContext,
                    "Success ${response!!.body()}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(TAG, "onResponse: ${response.body()}")
            }

            override fun onFailure(call: Call<UploadResponse>?, t: Throwable?) {
                Log.d(TAG, "onFailure: $t")
                Log.d(TAG, "onFailure: ${call.toString()}")
                Toast.makeText(applicationContext, "Failure ${t!!.message}", Toast.LENGTH_SHORT)
                    .show()
            }

        })
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )
            return;
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {

        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            17
        )
    }

    fun uploadImg(view: View) {
        hitapi(backupFile!!)
    }

    private fun getRealPathFromURI(contentUri: Uri): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val loader = CursorLoader(this, contentUri, proj, null, null, null)
        val cursor: Cursor = loader.loadInBackground()!!
        val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val result: String = cursor.getString(column_index)
        cursor.close()
        return result
    }
}