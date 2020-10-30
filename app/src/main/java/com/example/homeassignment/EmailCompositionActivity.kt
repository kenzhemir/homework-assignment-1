package com.example.homeassignment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_email_composition.*

class EmailCompositionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_composition)
        if (!intent.hasExtra("email")) {
            finish()
        }
        val email = intent.getStringExtra("email")!!
        val number = intent.getStringExtra("number")
        val name = intent.getStringExtra("name")

        tv_email.text = email;
        tv_name.text = name
        tv_phone.text = number

        submit_email.setOnClickListener {
            sendEmail(
                tv_email.text.toString(),
                edit_title.text.toString(),
                edit_body.text.toString()
            );
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true);

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    fun sendEmail(email: String, subject: String, body: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email));
            putExtra(Intent.EXTRA_SUBJECT, subject);
            putExtra(Intent.EXTRA_TEXT, body);
            setType("message/rfc822");
        }

        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(emailIntent, 42);
        } else {
            startActivityForResult(
                Intent.createChooser(emailIntent, "Choose your email app: "),
                42
            );
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 42) {
            setResult(42)
            finish()
        }
    }
}