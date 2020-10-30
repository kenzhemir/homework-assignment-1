package com.example.homeassignment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAG: String = this::class.java.name;
    private lateinit var viewAdapter: ContactsAdapter;

    var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fromColumns: Array<String>? = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        )
        val toViews: IntArray? = intArrayOf(R.id.tv_contact_name)
        val adapter = SimpleCursorAdapter(
            this,
            R.layout.contacts_view,
            null,
            fromColumns,
            toViews,
            0
        )
        viewAdapter = ContactsAdapter(adapter, this, this::onContactClickHandler)
        rv_contacts_list.adapter = viewAdapter
        rv_contacts_list.layoutManager = LinearLayoutManager(this);

        requestPermissionsButton.setOnClickListener { requester() }

        requester()
    }

    private fun requester() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                1
            )
        } else {
            filterContacts();
        }
    }

    fun initializeData() {
//        val cursor = getContactsCursor()

//        val fromColumns: Array<String>? = arrayOf(
//            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
//        )
//        val toViews: IntArray? = intArrayOf(R.id.tv_contact_name)
//        val adapter = SimpleCursorAdapter(
//            this,
//            R.layout.contacts_view,
//            cursor,
//            fromColumns,
//            toViews,
//            0
//        )
//        viewAdapter = ContactsAdapter(adapter, this, this::onContactClickHandler)
//        rv_contacts_list.adapter = viewAdapter
//        rv_contacts_list.layoutManager = LinearLayoutManager(this);
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search, menu)
        (menu.findItem(R.id.search_view).actionView as SearchView).apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    filterContacts(query)
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    filterContacts(newText)
                    return false
                }
            })
        }


        return true
    }

    fun filterContacts(query: String = "") {
        val contactsCursor = getContactsCursor(query)
        if (contactsCursor != null) {
            viewAdapter.cursorAdapter.changeCursor(contactsCursor)
            viewAdapter.notifyDataSetChanged();
        }
    }

    fun getContactsCursor(query: String = ""): Cursor? {
        val phone_uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val phone_columns = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        var selector: String? = null
        var selectorArgs: Array<String>? = null
        if (!query.isEmpty()) {
            selector = "LOWER(${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME}) LIKE ?"
            selectorArgs = arrayOf("%${query.toLowerCase(Locale.getDefault())}%")
        }
        return contentResolver.query(
            phone_uri,
            phone_columns,
            selector,
            selectorArgs,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )
    }

    fun getEmailCursorByContactId(contactId: String): String? {
        val email_uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI
        val email_columns = arrayOf(
            ContactsContract.CommonDataKinds.Email.CONTACT_ID,
            ContactsContract.CommonDataKinds.Email.ADDRESS
        )
        val selectionString = "${ContactsContract.CommonDataKinds.Email.CONTACT_ID}=?"

        val cursor: Cursor? = contentResolver.query(
            email_uri, email_columns, selectionString, arrayOf(
                contactId
            ), null
        )
        Log.i(TAG, contactId)
        return cursor?.let {
            val index = cursor.getColumnIndex(email_columns[1])
            var email: String? = null
            if (cursor.count > 0) {
                cursor.moveToNext()
                email = cursor.getString(index)
            }
            cursor.close()
            email
        }
    }

    fun onContactClickHandler(contactId: String, name: String, number: String): Unit {
        val i = Intent(this, EmailCompositionActivity::class.java)
        val email = getEmailCursorByContactId(contactId)
        if (email != null) {
            i.putExtra("email", email)
            i.putExtra("name", name)
            i.putExtra("number", number)

            startActivityForResult(i, 1)
        } else {
            showToast("No email found for this contact!")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == 42) {
                showToast("Yay! Email was sent!")
            } else {
                showToast("Email was not sent :(")
            }
        }
    }

    fun showToast(text: String) {
        toast?.cancel()
        toast = Toast.makeText(this, text, Toast.LENGTH_SHORT).also { it.show() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionsButton.visibility = View.GONE
                filterContacts()
            } else {
                requestPermissionsButton.visibility = View.VISIBLE
            }
        }
    }
}