package com.lmizuno.smallnotesmanager.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.lmizuno.smallnotesmanager.R


//https://developer.android.com/develop/ui/views/components/dialogs
class DeleteDialogFragment(
    private val message: String, val confirm: () -> Unit, val cancel: () -> Unit
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(message)
                .setPositiveButton(getString(R.string.remove)) { dialog, id ->
                    confirm()
                }.setNegativeButton(getString(R.string.cancel)) { dialog, id ->
                    cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}