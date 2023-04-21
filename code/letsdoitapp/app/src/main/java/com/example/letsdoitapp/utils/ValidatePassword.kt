package com.example.letsdoitapp.utils

import java.util.regex.Matcher
import java.util.regex.Pattern

class ValidatePassword {
    companion object{
        private var pat: Pattern?= null
        private var mat: Matcher?= null

        fun isPassword(password:String): Boolean{
            pat = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$")
            mat = pat!!.matcher(password)
            return mat!!.find()
        }
    }
}