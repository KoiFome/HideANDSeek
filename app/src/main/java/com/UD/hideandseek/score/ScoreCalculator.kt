package com.UD.hideandseek.score

object ScoreCalculator {
    /**
     * score = 700 * factorTiempo + 300 * factorPrecision
     *   factorTiempo    = (total - transcurrido) / total   en [0,1]
     *   factorPrecision = 1 - (errorGrados / 90)            en [0,1]
     * Rango: 0..1000 pts. Búsqueda rápida y precisa => cercano a 1000.
     */
    fun compute(elapsed: Float, total: Float, precisionDeg: Float): Int {
        val timeFactor = ((total - elapsed) / total).coerceIn(0f, 1f)
        val precisionFactor = (1f - precisionDeg / 90f).coerceIn(0f, 1f)
        return (timeFactor * 700 + precisionFactor * 300).toInt()
    }
}

