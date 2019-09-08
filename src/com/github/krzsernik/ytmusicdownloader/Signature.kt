package com.github.krzsernik.ytmusicdownloader

import java.io.IOException
import java.util.*

object Signature {
    private var baseUrl: String? = null
    private val signatureSteps = ArrayList<Int>()

    private val signatureSetterPatterns = listOf(
            """.\.s&&.\.set\(.\.sp,encodeURIComponent\(([a-zA-Z0-9]+)\(decodeURIComponent\(.\.s\)\)\)\)""".toRegex(),
            """.&&.\.set\(.,encodeURIComponent\(([a-zA-Z0-9]+)\(decodeURIComponent\(.\)\)\)\)""".toRegex())

    fun setBaseUrl(baseUrl: String) {
        Signature.baseUrl = baseUrl

        try {
            val req = Request(baseUrl, "GET")
            req.send()

            val javascript = req.content

            Signature.parseJavaScript(javascript)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun parseJavaScript(code: String) {
        var found = false
        var encodingFunction: String? = null

        outer@ for (pattern in signatureSetterPatterns) {
            val mat = pattern.find(code)

            if(mat != null && mat.groupValues.size > 1) {
                found = true
                encodingFunction = mat.groupValues[1]
                break@outer
            }
        }

        if (found) {
            var function = code.split((encodingFunction!! + "=function\\(a\\)\\{").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            function = function.split(";return a\\.join\\(\"\"\\)\\};".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

            val signatureSteps = ArrayList<String>(listOf(*function.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
            signatureSteps.remove(signatureSteps.get(0)) // remove `a.split("")`

            val functionsObjectName = signatureSteps.get(0).split("\\.".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0]
            var functionsObject = code.split((functionsObjectName + "=\\{").toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1]
            functionsObject = functionsObject.split("\\}\\};".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0]

            val funcMap = HashMap<String, Int>()
            for (encoder in functionsObject.split("\\},\\s*".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()) {
                val name = encoder.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0]

                var methodIndex = -1
                if (encoder.contains("a.splice(0,b)")) {
                    methodIndex = 0
                } else if (encoder.contains("var c=a[0];a[0]=a[b%a.length];a[b%a.length]=c")) {
                    methodIndex = 1
                } else if (encoder.contains("a.reverse()")) {
                    methodIndex = 2
                }

                funcMap[name] = methodIndex
            }

            for (step in signatureSteps) {
                val name = step.split("[\\.\\(]".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1]
                val arg = Integer.parseInt(step.split("[,\\)]".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1])

                Signature.signatureSteps.add((funcMap.getOrDefault(name, 0) shl 7) + arg)
            }
        } else {
            System.err.println("\n------------------------")
            System.err.println("Pattern hasnt been found")
            System.err.println(baseUrl)
            System.err.println("------------------------\n")
        }
    }

    // function(a,b){a.splice(0,b)}
    fun Splice(s: String, b: Int): String {
        return s.substring(b)
    }

    // function(a,b){var c=a[0];a[0]=a[b%a.length];a[b%a.length]=c}
    fun Replace(s: String, b: Int): String {
        val a = s.toCharArray()
        val c = a[0]
        a[0] = a[b % a.size]
        a[b % a.size] = c

        return String(a)
    }

    // function(a){a.reverse()}
    fun Reverse(s: String, b: Int): String {
        val a = StringBuilder(s)
        return a.reverse().toString()
    }

    fun createSignature(s: String): String {
        var s = s
        for (step in signatureSteps) {
            when (step shr 7) {
                0 -> s = Splice(s, step and 127)
                1 -> s = Replace(s, step and 127)
                2 -> s = Reverse(s, step and 127)
            }
        }

        return s
    }
}
