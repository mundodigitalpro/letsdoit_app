package com.example.letsdoitapp.utils

import java.util.regex.Matcher
import java.util.regex.Pattern


class ValidateEmail {
    companion object{
        private var pat: Pattern?= null
        private var mat: Matcher?= null

        fun isEmail(email:String): Boolean{
            pat = Pattern.compile("^[\\w\\-_+]+(\\.[\\w\\-_]+)*@([A-Za-z\\d-]+\\.)+[A-Za-z]{2,4}$")
            mat = pat!!.matcher(email)
            return mat!!.find()
        }
    }
}