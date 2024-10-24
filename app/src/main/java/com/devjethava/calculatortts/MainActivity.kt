package com.devjethava.calculatortts

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.ContextMenu
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.devjethava.calculatortts.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var tts: TextToSpeech
    private lateinit var display: TextView
    private var currentExpression = StringBuilder()
    private var isOperatorEntered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize TTS
        tts = TextToSpeech(this, this)

        // Initialize display
        display = findViewById(R.id.tvDisplay)

        setupButtons()
        clearDisplay()

    }

    private fun setupButtons() {
        // Number buttons (assuming buttons are named btn0, btn1, etc. in layout)
        for (i in 0..9) {
            val button = when (i) {
                0 -> binding.btn0
                1 -> binding.btn1
                2 -> binding.btn2
                3 -> binding.btn3
                4 -> binding.btn4
                5 -> binding.btn5
                6 -> binding.btn6
                7 -> binding.btn7
                8 -> binding.btn8
                9 -> binding.btn9
                else -> throw IllegalStateException("Invalid button index")
            }
            button.setOnClickListener {
                appendInput(i.toString())
                speak(i.toString())
            }
        }

        // Operator buttons
        binding.apply {
            btnPlus.setOnClickListener { handleOperator("+", "plus") }
            btnMinus.setOnClickListener { handleOperator("-", "minus") }
            btnMultiply.setOnClickListener { handleOperator("*", "multiply") }
            btnDivide.setOnClickListener { handleOperator("/", "divide by") }
            btnEquals.setOnClickListener { calculateResult() }
            btnClear.setOnClickListener { clearDisplay() }
            btnDelete.setOnClickListener { handleBackspace() }
            btnDot.setOnClickListener {
                appendInput(".")
                speak("point")
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            speak("Calculator ready. Result: 0")
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // Handle CTRL+M for menu
        if (keyCode == KeyEvent.KEYCODE_M && event.isCtrlPressed) {
            openContextMenu(display)
            return true
        }

        when (keyCode) {
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_EQUALS -> {
                calculateResult()
                return true
            }

            KeyEvent.KEYCODE_DEL -> {
                if (event.isShiftPressed) {  // Handle Delete key
                    clearDisplay()
                } else {  // Handle Backspace
                    handleBackspace()
                }
                return true
            }

            KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_9 -> {
                val number = (keyCode - KeyEvent.KEYCODE_0).toString()
                appendInput(number)
                speak(number)
                return true
            }

            KeyEvent.KEYCODE_PERIOD -> {
                appendInput(".")
                speak("point")
                return true
            }
        }

        // Handle operators
        when (event.unicodeChar.toChar()) {
            '+' -> handleOperator("+", "plus")
            '-' -> handleOperator("-", "minus")
            '*' -> handleOperator("*", "multiply")
            '/' -> handleOperator("/", "divide by")
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun handleOperator(operator: String, spokenOperator: String) {
        if (!isOperatorEntered) {
            appendInput(" $operator ")
            speak(spokenOperator)
            isOperatorEntered = true
        }
    }

    private fun appendInput(input: String) {
        currentExpression.append(input)
        updateDisplay()
    }

    private fun updateDisplay() {
        binding.tvDisplay.text = currentExpression.toString()
    }

    private fun handleBackspace() {
        if (currentExpression.isNotEmpty()) {
            val deleted = currentExpression.last()
            currentExpression.deleteCharAt(currentExpression.length - 1)
            updateDisplay()
            speak("deleted $deleted")
        }
    }

    private fun clearDisplay() {
        currentExpression.clear()
        display.text = "0"
        speak("Clear all. Result: 0")
        isOperatorEntered = false
    }

    private fun calculateResult() {
        try {
            val expression = currentExpression.toString()
            val parts = expression.split(" ")

            if (parts.size != 3) {
                speak("error")
                return
            }

            val num1 = parts[0].toDouble()
            val operator = parts[1]
            val num2 = parts[2].toDouble()

            val result = when (operator) {
                "+" -> num1 + num2
                "-" -> num1 - num2
                "*" -> num1 * num2
                "/" -> {
                    if (num2 == 0.0) throw ArithmeticException("Division by zero")
                    num1 / num2
                }

                else -> throw IllegalArgumentException("Invalid operator")
            }

            currentExpression.clear()
            currentExpression.append(result)
            updateDisplay()
            speak("Result: $result")
            isOperatorEntered = false

        } catch (e: Exception) {
            speak("error")
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.apply {
            add(0, 1, 0, "Plus").setShortcut('p', 'p')
            add(0, 2, 0, "Minus").setShortcut('m', 'm')
            add(0, 3, 0, "Multiply").setShortcut('l', 'l')
            add(0, 4, 0, "Divide by").setShortcut('d', 'd')
            add(0, 5, 0, "Clear display").setShortcut('c', 'c')
            add(0, 6, 0, "Exit").setShortcut('x', 'x')
        }
        registerForContextMenu(binding.tvDisplay)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            1 -> {
                handleOperator("+", "plus"); true
            }

            2 -> {
                handleOperator("-", "minus"); true
            }

            3 -> {
                handleOperator("*", "multiply"); true
            }

            4 -> {
                handleOperator("/", "divide by"); true
            }

            5 -> {
                clearDisplay(); true
            }

            6 -> {
                finish(); true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}