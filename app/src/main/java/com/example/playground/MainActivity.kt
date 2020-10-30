package com.example.playground

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.playground.databinding.CustomToastBinding
import com.example.playground.dialog.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: CustomToastBinding
    private val factory by lazy { SharedViewModelFactory() }
    private val viewModel by lazy {
        ViewModelProvider(this, factory).get(SharedViewModel::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CustomToastBinding.inflate(layoutInflater)

        setContentView(R.layout.activity_main)
        setupClickListeners()

        viewModel.name.observe(this, Observer {
            btn_custom_dialog_input.text = it
        })
    }

    fun toastMsg(msg: String) {
        binding.tvToast.text = msg
        CustomToast(msg, applicationContext, binding.root).show()
    }

    fun snackbar(action: () -> Unit): Snackbar {
        val snack = Snackbar.make(
            this.findViewById(android.R.id.content),
            "I am staying here indefinitely",
            Snackbar.LENGTH_INDEFINITE
        )
        snack.setAction("Show Toast") { action() }
        return snack
    }

    private fun setupClickListeners() {
        btn_custom_dialog_input.setOnClickListener {
            FragmentDialogInput().show(supportFragmentManager, "TAG_CUSTOM")
        }
        btn_alert_dialog.setOnClickListener {
            CustomStockAlertDialog().show(supportFragmentManager, "TAG_CUSTOM")
        }

        btn_simple_dialog.setOnClickListener {
            FragmentCustomDialog().show(supportFragmentManager, "TAG_CUSTOM")
        }

        btn_custom_toast.setOnClickListener {
            toastMsg("This is a custom toast")
        }
        btn_snackbar.setOnClickListener {
            snackbar {
                toastMsg("Custom Toast via Snack")
            }.show()
        }
    }
}