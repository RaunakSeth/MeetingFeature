package com.example.meetingfeature

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.DocumentList
import io.appwrite.models.User
import io.appwrite.services.Account
import io.appwrite.services.Databases

object MeetViewModelClass:ViewModel() {
    lateinit var client: Client
    lateinit var account: Account


    lateinit var databaseId: String
    lateinit var collectionId: String
    lateinit var documentId: String
    lateinit var fileId: String
    lateinit var bucketId: String
    lateinit var userId: String
    lateinit var functionId: String
    fun init(context: Context) {
        client = Client(context)
            .setEndpoint("https://cloud.appwrite.io/v1")
            .setProject("65a13ea18d5f01890c97")
        account= Account(client)
    }
    suspend fun getAccount(): User<Map<String, Any>>
    {
        return account.get()
    }
    suspend fun createAccount(context: Context, username: String, mail: String, password: String):Boolean {
        try {
            account.create(userId=username, email=mail, password=password)
            Toast.makeText(context,"Registered $username", Toast.LENGTH_LONG)
            return true
        } catch (e: AppwriteException) {
            Log.d("Register Account", e.message.toString())
            return false
        }
    }

    suspend fun login(context: Context, username: String?=null, mail: String, password: String):Boolean
    {
        try {
            val session = account.createEmailSession(
                email = mail,
                password = password
            )
            Toast.makeText(context,"Logged in $username", Toast.LENGTH_LONG)
            return true
        }
        catch (e: AppwriteException){
            Log.d("Login Account", e.message.toString())
            return false
        }
    }
    suspend fun createverificaton(username: String,password: String)
    {
        account = Account(client)
        account.updateVerification(username,password)
    }
    suspend fun deleteAllSessions()
    {
        account.deleteSessions()
    }
    suspend fun onLogout() {
        account.deleteSession("current")
    }
    suspend fun storetoDatabase(MeetDetails:MeetDetails,meets:MutableList<MeetDetails>,context: Context):Boolean
    {
        val databases = Databases(client)
        for(i in meets)
        {
            if(MeetDetails==i)
            {
                Toast.makeText(context,"Failed due to duplicate value",Toast.LENGTH_LONG)
                return false
            }
        }
        try {
            val document = databases.createDocument(
                databaseId = "65a141604e7011fe63b9",
                collectionId = "65a14182557903534d65",
                documentId = ID.unique(),
                data = mapOf(
                    "MeetDomain" to MeetDetails.MeetDomain,
                    "MeetPlace" to MeetDetails.MeetPlace,
                    "MeetDescription" to MeetDetails.MeetDescription,
                    "MeetYear" to MeetDetails.MeetYear,
                    "MeetDateTime" to MeetDetails.LocalDateTime),
            )
            return true
        } catch (e: Exception) {
            Log.e("Appwrite", "Error: " + e.message)
            Toast.makeText(context,"Failed due to ${e.message}",Toast.LENGTH_LONG)
            return false
        }
    }
    suspend fun getfromDatabase():DocumentList<Map<String,Any>>
    {
        lateinit var response:DocumentList<Map<String,Any>>
        val databases = Databases(client)
        try {
                response = databases.listDocuments(
                databaseId = "65a141604e7011fe63b9",
                collectionId = "65a14182557903534d65",)
            Log.d("Success","$response")
            return response
        }
        catch (e:Exception)
        {
            Log.e("Appwrite","Error: " + e.message)
        }
        return response
    }
   
}
