package com.example.bg

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MyInterface {
    @Multipart
    @POST("/WebApi/index.php/welcome/do_upload")
    fun uploadUserImg(@Part img: MultipartBody.Part, @Part("id") userId: Int): Call<UploadResponse>

}