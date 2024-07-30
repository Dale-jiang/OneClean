package com.kk.newcleanx.ui.common.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.kk.newcleanx.R
import com.kk.newcleanx.databinding.DialogLoadingAdLayoutBinding
import com.kk.newcleanx.utils.dp2px
import com.kk.newcleanx.utils.getScreenWidth


class AdLoadingDialog(private val context: Context) {
    fun showDialog(): AlertDialog {
        val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
        val inflater = LayoutInflater.from(context)
        val binding = DialogLoadingAdLayoutBinding.inflate(inflater)

        builder.setView(binding.root)
        val dialog = builder.setCancelable(false).create()

        dialog.show()
        dialog.window?.setLayout(
            context.getScreenWidth() - 46.dp2px(), WindowManager.LayoutParams.WRAP_CONTENT
        )

        return dialog
    }

}