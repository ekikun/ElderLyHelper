package com.eki.common.widget

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.eki.common.R

class HelpDialogFragment: DialogFragment() {

    private lateinit var listener: NoticeDialogListener

    private lateinit var setViewFunction: DialogViewGetter

    private lateinit var dialogView:View

    var alertDialog:AlertDialog? = null

    interface DialogViewGetter{
        fun getDialogView():View
    }

    interface NoticeDialogListener {
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            setViewFunction = context as DialogViewGetter
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       return activity?.let {
            val builder = AlertDialog.Builder(it)
            dialogView = setViewFunction.getDialogView()
            builder.setView(dialogView)
                    .setNegativeButton(R.string.button_dialog_cancel, DialogInterface.OnClickListener(){
                        dialog, id ->
                       listener.onDialogNegativeClick(this)
                       getDialog()?.cancel()
                    })
           builder.create().also {
               alertDialog = it
           }
        }?: throw IllegalStateException("Activity cannot be null")
    }
}