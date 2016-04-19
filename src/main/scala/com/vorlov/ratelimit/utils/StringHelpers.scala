package com.vorlov.ratelimit.utils

object StringHelpers {

  implicit class StringHelpersRichString(str: String) {

    def isAllDigits = str forall Character.isDigit

  }

}
