package com.reas.trackerviewer.locationHistory

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.reas.trackerviewer.R


class BottomSheetDialogFragment : BottomSheetDialogFragment() {



    val locationViewModel: LocationViewModel by lazy {
        ViewModelProvider(this).get(LocationViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.setOnShowListener { dialog ->
            val bottomSheet = dialog as BottomSheetDialog
            val bottomSheetInternal = bottomSheet.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheetInternal?.minimumHeight =
                Resources.getSystem().displayMetrics.heightPixels
        }
    }

    companion object {
        fun newInstance(): BottomSheetDialogFragment {
            return BottomSheetDialogFragment()
        }
    }
}