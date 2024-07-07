package com.rktechnohub.sugarbashprogressapp.setting.freagment

import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.flask.colorpicker.builder.ColorPickerClickListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.rktechnohub.sugarbashprogressapp.R
import com.rktechnohub.sugarbashprogressapp.setting.activity.CustomColorActivity
import com.rktechnohub.sugarbashprogressapp.utils.ColorCustomizeClass


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [SettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingFragment : Fragment() {
    private val ARG_MAPLE = "is_maple"
    private var view: View? = null
    private lateinit var tvColor: AppCompatTextView
    private lateinit var clColor: ConstraintLayout
    var selectedColor = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*arguments?.let {
            isMaple = it.getBoolean(ARG_MAPLE)
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_setting, container, false)
        init()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setUpRecyclerView()
    }

    private fun init(){
        tvColor = view?.findViewById(R.id.tv_color)!!
        clColor = view?.findViewById(R.id.cl_color)!!


        clColor.setOnClickListener{
            showCustomPickerDialog()

        }
    }

    fun showCustomPickerDialog(){

        val inflater = getLayoutInflater()
        val dialogView: View = inflater.inflate(R.layout.dialog_custom_color, null)

        // Create the dialog

        // Create the dialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()

        var selectedOption = 1
        // Set up the dialog components (optional)

        // Set up the dialog components (optional)
        val ivOp1 = dialogView.findViewById<AppCompatImageView>(R.id.iv_op_1)
        val ivOp2 = dialogView.findViewById<AppCompatImageView>(R.id.iv_op_2)
        val ivOp3 = dialogView.findViewById<AppCompatImageView>(R.id.iv_op_3)
        val ivOp4 = dialogView.findViewById<AppCompatImageView>(R.id.iv_op_4)
        val btnOK = dialogView.findViewById<AppCompatButton>(R.id.btn_ok)
        val btnCancel = dialogView.findViewById<AppCompatButton>(R.id.btn_cancel)

        ivOp1.setOnClickListener {
            selectedOption = 1
            ivOp1.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vector_radio_checked))
            ivOp2.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vector_radio_unchecked))
            ivOp3.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vector_radio_unchecked))
            ivOp4.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vector_radio_unchecked))
        }

        ivOp2.setOnClickListener {
            selectedOption = 2
            ivOp2.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vector_radio_checked))
            ivOp1.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vector_radio_unchecked))
            ivOp3.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vector_radio_unchecked))
            ivOp4.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vector_radio_unchecked))
        }

        ivOp3.setOnClickListener {
            selectedOption = 3
            ivOp3.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vector_radio_checked))
            ivOp1.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vector_radio_unchecked))
            ivOp2.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vector_radio_unchecked))
            ivOp4.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vector_radio_unchecked))
        }
        ivOp4.setOnClickListener {
            selectedOption = 4
            ivOp4.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vector_radio_checked))
            ivOp1.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vector_radio_unchecked))
            ivOp2.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vector_radio_unchecked))
            ivOp3.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vector_radio_unchecked))
        }

        // Set button click listener
        btnOK.setOnClickListener { // Handle button click
            when(selectedOption){
                1 -> ColorCustomizeClass.changeColorOpOne()
                2 -> ColorCustomizeClass.changeColorOpTwo()
                3 -> ColorCustomizeClass.changeColorOpThree()
                4 -> {
                    val intent = Intent(requireContext(), CustomColorActivity::class.java)
                    startActivity(intent)
                }
            }
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { // Handle button click


            // Close the dialog
            dialog.dismiss()
        }

        // Show the dialog

        // Show the dialog
        dialog.show()
    }




    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(isMaple: Boolean) =
            SettingFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_MAPLE, isMaple)
                }
            }
    }
}