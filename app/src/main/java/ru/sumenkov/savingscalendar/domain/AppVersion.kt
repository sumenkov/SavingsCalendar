package ru.sumenkov.savingscalendar.domain

object AppVersion {
    fun isNewer(remoteVersion: String, currentVersion: String): Boolean {
        return compare(remoteVersion, currentVersion) > 0
    }

    private fun compare(left: String, right: String): Int {
        val leftParts = left.toVersionParts()
        val rightParts = right.toVersionParts()
        val maxSize = maxOf(leftParts.size, rightParts.size)

        for (index in 0 until maxSize) {
            val leftPart = leftParts.getOrElse(index) { 0 }
            val rightPart = rightParts.getOrElse(index) { 0 }
            if (leftPart != rightPart) return leftPart.compareTo(rightPart)
        }

        return 0
    }

    private fun String.toVersionParts(): List<Int> {
        return trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore('-')
            .substringBefore('+')
            .split('.')
            .mapNotNull { part -> part.toIntOrNull() }
    }
}
