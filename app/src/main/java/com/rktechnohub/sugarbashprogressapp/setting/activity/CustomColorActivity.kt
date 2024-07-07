package com.rktechnohub.sugarbashprogressapp.setting.activity

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.flask.colorpicker.builder.ColorPickerClickListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.rktechnohub.sugarbashprogressapp.R
import com.rktechnohub.sugarbashprogressapp.utils.ColorCustomizeClass
import com.rktechnohub.sugarbashprogressapp.utils.ColorCustomizeClass.setCustomProgressColor

class CustomColorActivity : AppCompatActivity() {
    var selectedColorOne = 0
    var selectedColorTwo = 0
    var selectedColorThree = 0

    private lateinit var clOp1: ConstraintLayout
    private lateinit var clOp2: ConstraintLayout
    private lateinit var clOp3: ConstraintLayout
    private lateinit var progressBar1: ProgressBar
    private lateinit var progressBar2: ProgressBar
    private lateinit var progressBar3: ProgressBar
    private lateinit var btnDone: AppCompatButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_custom_color)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        registerViews()
    }

    fun registerViews(){
        clOp1 = findViewById(R.id.iv_op_1)
        clOp2 = findViewById(R.id.iv_op_2)
        clOp3 = findViewById(R.id.iv_op_3)
        progressBar1 = findViewById(R.id.progress_op_1)
        progressBar2 = findViewById(R.id.progress_op_2)
        progressBar3 = findViewById(R.id.progress_op_3)
        btnDone = findViewById(R.id.btn_done)

        clOp1.setOnClickListener {
            showDialogForColorPicker(1)
        }

        clOp2.setOnClickListener {
            showDialogForColorPicker(2)
        }

        clOp3.setOnClickListener {
            showDialogForColorPicker(3)
        }

        btnDone.setOnClickListener {
            ColorCustomizeClass.setCustomColors(selectedColorOne, selectedColorTwo, selectedColorThree)
            finish() }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun showDialogForColorPicker(option: Int){
        ColorPickerDialogBuilder.with(this)
            .setTitle("Pick a color")
//                .se("MyColorPickerDialog")
            .setOnColorSelectedListener {
//                val selectedColor =  "0x" + it.toHexString(HexFormat.Default)

            }
            .setPositiveButton("Ok", object : ColorPickerClickListener {
                override fun onClick(
                    d: DialogInterface?,
                    lastSelectedColor: Int,
                    allColors: Array<out Int>?
                ) {
                    val selected = "0x" + lastSelectedColor.toHexString(HexFormat.Default)
                    when(option){
                        1 -> {
                            selectedColorOne = lastSelectedColor
//                            val color: Int = Color.parseColor(selectedColorOne)
                            clOp1.setBackgroundColor(lastSelectedColor)
                            progressBar1.setCustomProgressColor(lastSelectedColor)
                        }
                        2 -> {
                            selectedColorTwo = lastSelectedColor
//                            val color: Int = Color.parseColor(selectedColorTwo)
                            clOp2.setBackgroundColor(lastSelectedColor)
                            progressBar2.setCustomProgressColor(lastSelectedColor)
                        }
                        3 -> {
                            selectedColorThree = lastSelectedColor
//                            val color: Int = Color.parseColor(selectedColorThree)
                            clOp3.setBackgroundColor(lastSelectedColor)
                            progressBar3.setCustomProgressColor(lastSelectedColor)
                        }
                    }
                }

            })
            .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {

                }

            })
            .build().show()
    }
}