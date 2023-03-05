package com.example.calc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var etExpression: EditText
    private lateinit var buttonEquals: Button
    var canAddOperation = false
    var canAddDecimal = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonEquals = findViewById(R.id.buttonEquals)

        etExpression = findViewById(R.id.etExpression)
        disableKeyBoard()

        buttonEquals.setOnClickListener {
            if(isValid(etExpression.text.toString()))
                tvResult.text = calculateResult()
            else{
                Toast.makeText(applicationContext, "Invalid Expression", Toast.LENGTH_SHORT).show()
            }
        }


    }
    private fun disableKeyBoard() {
        etExpression.setShowSoftInputOnFocus(false)
    }

    fun numberAction(view: View) {
        if(view is Button) {
            if(view.text == ".") {
                if(canAddDecimal)
                    append(view.text)
                canAddDecimal = false
            }
            else
                append(view.text)

            canAddOperation = true
        }
    }

    fun operationAction(view: View) {
        if(view is Button && canAddOperation) {
            append(view.text)
            canAddOperation = false
            canAddDecimal = true
        }
    }

    private fun append(text: CharSequence?) {
        val start = Math.max(etExpression.selectionStart, 0)
        val end = Math.max(etExpression.getSelectionEnd(), 0)
        if (text != null) {
            etExpression.text.replace(Math.min(start, end), Math.max(start, end),
                text, 0, text.length)
        }
    }

    fun allClearAction(view: View) {
        etExpression.setText("")
        tvResult.text = ""
    }

    fun backSpaceAction(view: View) {
        val length = etExpression.text.length
        if(length > 0) {
            etExpression.setText(etExpression.text.subSequence(0, length - 1))
            etExpression.setSelection(etExpression.length())
        }
    }


    private fun calculateResult(): String {
        val digitsOperators = digitsOperators(etExpression.text.toString())
        if(digitsOperators.isEmpty()) return ""

        val timesDivision = timesDivisionCalculate(digitsOperators)
        if(timesDivision.isEmpty()) return ""

        val result = addSubtractCalculate(timesDivision)
        return result.toString()
    }

    private fun addSubtractCalculate(passesList: MutableList<Any>): Float {
        var result = passesList[0] as Float
        for(i in passesList.indices) {
            if(passesList[i] is Char && i != passesList.lastIndex) {
                val operator = passesList[i]
                val nextDigit = passesList[i+1].toString().toFloat()
                if(operator == '+')
                    result += nextDigit
                if(operator == '-')
                    result -= nextDigit
            }
        }
        return result
    }

    private fun timesDivisionCalculate(passedList: MutableList<Any>): MutableList<Any> {
        var list = passedList
        while(list.contains('×') || list.contains('÷') || list.contains('%')) {
            list = calcTimesDiv(list)
        }
        return list
    }

    private fun calcTimesDiv(passedList: MutableList<Any>): MutableList<Any> {
        val newList = mutableListOf<Any>()
        var restartIndex = passedList.size

        for(i in passedList.indices) {
            if(passedList[i] is Char && i != passedList.lastIndex && i<restartIndex) {
                val operator = passedList[i]
                val prevDigit = passedList[i-1].toString().toFloat()
                val nextDigit= passedList[i+1].toString().toFloat()
                when(operator) {
                    '÷' -> {
                        newList.add(prevDigit / nextDigit)
                        restartIndex = i+1
                    }
                    '×' -> {
                        newList.add(prevDigit * nextDigit)
                        restartIndex = i+1
                    }
                    '%' -> {
                        newList.add((prevDigit/100) * nextDigit)
                        restartIndex = i+1
                    }
                    else -> {
                        newList.add(prevDigit)
                        newList.add(operator)
                    }
                }
            }
            if(i>restartIndex) {
                newList.add(passedList[i])
            }

        }

        return newList
    }

    private fun digitsOperators(text: String): MutableList<Any> {
        val list = mutableListOf<Any>()
        var currentDigit = ""
        for(character in text) {
            if(character.isDigit() || character == '.') {
                currentDigit += character
            }
            else{
                list.add(currentDigit.toFloat())
                currentDigit =""
                list.add(character)
            }
        }
        if(currentDigit != "") {
            list.add(currentDigit)
        }
        return list
    }

    private fun isValid(infix:String):Boolean{
        val n=infix.length;
        if(infix.length==0 || infix[0]==')' || infix[n-1]=='(') return false;
        val operator = arrayOf('+','-','x','/','^')
        if(infix[n-1] in operator) return false;

        //now we need to iteratre over string to check all operators are correct or not
        for(i in 0 until n){
            if(infix[i] in operator){
                if(infix[i]!='-' && infix[i-1]=='(') return false;
                if(infix[i+1]==')') return false;
            }
        }
        //handling exception like 24+56(24+56) when a open bracket in in middle of statement then there should be a operator
        for(i in 1 until n-1){
            if(infix[i]=='(' && infix[i-1] !in operator)   return false;
            if(infix[i]==')' && (infix[i+1] !in operator))   return false;
        }
        //check if the brackets are correct or not
        val tstack = Stack<Char>()
        for(i in 0 until n){
            if(infix[i]=='(')   tstack.push(infix[i])
            else if(infix[i]==')'){
                if(tstack.isEmpty())    return false;
                else tstack.pop();
            }
        }
        return tstack.isEmpty()
    }
}

