package com.routee.android.core.data.appnotice.model

/**
 * 앱 버전 (major.minor.patch)
 */
data class AppVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
) : Comparable<AppVersion> {
    override fun compareTo(other: AppVersion): Int =
        compareValuesBy(this, other, AppVersion::major, AppVersion::minor, AppVersion::patch)

    companion object {
        /**
         * 문자열을 버전으로 변환
         *
         * @param value  "1.2.3" 형식. 빠진 자리는 0 ("1.0" = 1.0.0)
         * @return 숫자가 아니거나 자리가 넘치면 null
         */
        fun parse(value: String): AppVersion? {
            val parts = value.trim().split(".")
            if (parts.size > 3 || !parts.all { it.isNumber() }) return null
            val numbers = parts.map { it.toIntOrNull() ?: return null }
            return AppVersion(
                major = numbers[0],
                minor = numbers.getOrElse(1) { 0 },
                patch = numbers.getOrElse(2) { 0 },
            )
        }

        private fun String.isNumber(): Boolean = isNotEmpty() && all { it in '0'..'9' }
    }
}
