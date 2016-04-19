package com.vorlov.ratelimit.utils

import StringHelpers._

import org.scalatest.{Matchers, WordSpec}

class StringHelpersSpec extends WordSpec with Matchers  {

  "StringHelpers" should {

    "correctly apply isAllDigits function" in {

      "is not a number".isAllDigits should not be (true)
      "0.1".isAllDigits should not be (true)
      "01".isAllDigits should be (true)
      "-01".isAllDigits should not be (true)

    }

  }


}
