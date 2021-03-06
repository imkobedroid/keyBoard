package loan.moerlong.com.customkeyboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import loan.moerlong.com.customkeyboard.widget.KeyBoardUtil


/**
 *Created by Dsh
 */
class MainActivity : AppCompatActivity() {
    private lateinit var et1: EditText
    private lateinit var et2: EditText
    private lateinit var et3: EditText
    private lateinit var button: Button

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        et1 = findViewById(R.id.et1)
        et2 = findViewById(R.id.et2)
        et3 = findViewById(R.id.et3)
        button = findViewById(R.id.button)

        button.setOnClickListener {
            startActivity(Intent(this,BActivity::class.java))
        }


        et1.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                KeyBoardUtil(this@MainActivity, false, true).attachTo(et1)
            }
            false
        }

        et3.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                KeyBoardUtil(this@MainActivity, false, true).attachTo(et3)
            }
            false
        }

        et2.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                KeyBoardUtil(this@MainActivity, false, true).attachTo(et2)
            }
            false
        }


    }
}
