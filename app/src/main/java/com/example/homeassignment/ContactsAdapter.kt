package com.example.homeassignment

import android.content.Context
import android.provider.ContactsContract
import android.view.View
import android.view.ViewGroup
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.recyclerview.widget.RecyclerView

class ContactsAdapter(
    var cursorAdapter: SimpleCursorAdapter,
    val context: Context,
    val onContactClickHandler: (String, String, String) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {

    class ContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val view = cursorAdapter.newView(context, cursorAdapter.cursor, parent)
        return ContactsViewHolder(view);
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        cursorAdapter.cursor.moveToPosition(position);
        val cols = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val args = cols
            .map { cursorAdapter.cursor.getColumnIndex(it) }
            .map { cursorAdapter.cursor.getString(it) }

        holder.itemView.setOnClickListener {
            onContactClickHandler(args[0], args[1], args[2])
        }
        cursorAdapter.bindView(holder.itemView, context, cursorAdapter.cursor)
    }

    override fun getItemCount(): Int = cursorAdapter.count;
}