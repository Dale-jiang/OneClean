package com.kk.newcleanx.ui.common.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.kk.newcleanx.R
import com.kk.newcleanx.databinding.DialogCustomLayoutBinding
import com.kk.newcleanx.utils.dp2px
import com.kk.newcleanx.utils.getScreenWidth


class CustomAlertDialog(private val context: Context, private val cancelable: Boolean = true) {
    fun showDialog(
        title: String,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String,
        onPositiveButtonClick: (AlertDialog) -> Unit,
        onNegativeButtonClick: (AlertDialog) -> Unit
    ) {
        val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
        val inflater = LayoutInflater.from(context)
        val binding = DialogCustomLayoutBinding.inflate(inflater)

        binding.dialogTitle.text = title
        binding.dialogMessage.text = message
        binding.positiveButton.text = positiveButtonText
        if (negativeButtonText.isEmpty()) {
            binding.negativeButton.isVisible = false
        } else {
            binding.negativeButton.isVisible = true
            binding.negativeButton.text = negativeButtonText
        }

        builder.setView(binding.root)
        val dialog = builder.setCancelable(cancelable).create()

        binding.positiveButton.setOnClickListener {
            onPositiveButtonClick(dialog)
        }

        binding.negativeButton.setOnClickListener {
            onNegativeButtonClick(dialog)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(
            context.getScreenWidth() - 46.dp2px(), WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

}