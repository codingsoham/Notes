package com.example.notes

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND

fun shareOrder(context: Context,subject: String, summary: String){
    val intent = Intent(ACTION_SEND).apply{
        type= "text/plain"
        putExtra(Intent.EXTRA_SUBJECT,subject)
        putExtra(Intent.EXTRA_TEXT,summary)
    }

    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.delete)
        )
    )
}