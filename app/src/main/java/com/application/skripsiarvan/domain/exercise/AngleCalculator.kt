package com.application.skripsiarvan.domain.exercise

import android.util.Log
import com.application.skripsiarvan.domain.model.Keypoint
import com.application.skripsiarvan.domain.model.Person
import com.application.skripsiarvan.domain.model.BodyPart
import kotlin.math.sqrt

/**
 * Utility class untuk menghitung sudut antara tiga titik keypoint
 * Digunakan untuk deteksi gerakan latihan (Squat, Push-up)
 * 
 * Sesuai dengan Flowchart 4.4 Perhitungan Repetisi di skripsi
 */
object AngleCalculator {
    
    private const val TAG = "AngleCalculator"

    /**
     * Menghitung sudut antara tiga titik dalam derajat
     * 
     * @param first Titik pertama (misal: hip)
     * @param middle Titik tengah/vertex (misal: knee) - sudut dihitung di titik ini
     * @param last Titik terakhir (misal: ankle)
     * @return Sudut dalam derajat (0-180)
     */
    fun calculateAngle(first: Keypoint, middle: Keypoint, last: Keypoint): Double {
        // Vector dari middle ke first
        val vectorA = Pair(first.x - middle.x, first.y - middle.y)
        
        // Vector dari middle ke last
        val vectorB = Pair(last.x - middle.x, last.y - middle.y)
        
        // Hitung dot product
        val dotProduct = vectorA.first * vectorB.first + vectorA.second * vectorB.second
        
        // Hitung magnitude
        val magnitudeA = sqrt((vectorA.first * vectorA.first + vectorA.second * vectorA.second).toDouble())
        val magnitudeB = sqrt((vectorB.first * vectorB.first + vectorB.second * vectorB.second).toDouble())
        
        // Hitung sudut dalam radian
        if (magnitudeA == 0.0 || magnitudeB == 0.0) return 0.0
        
        val cosAngle = (dotProduct / (magnitudeA * magnitudeB)).coerceIn(-1.0, 1.0)
        val angleRad = kotlin.math.acos(cosAngle)
        
        // Konversi ke derajat
        return Math.toDegrees(angleRad)
    }

    /**
     * Menghitung sudut lutut kiri dari Person
     * Menggunakan keypoints: LEFT_HIP, LEFT_KNEE, LEFT_ANKLE
     */
    fun getLeftKneeAngle(person: Person): Double? {
        val hip = person.keypoints.getOrNull(BodyPart.LEFT_HIP)
        val knee = person.keypoints.getOrNull(BodyPart.LEFT_KNEE)
        val ankle = person.keypoints.getOrNull(BodyPart.LEFT_ANKLE)

        if (hip == null || knee == null || ankle == null) return null
        if (hip.score < 0.2f || knee.score < 0.2f || ankle.score < 0.2f) return null

        val angle = calculateAngle(hip, knee, ankle)
        // Sudut lutut yang valid secara anatomi: 30°–180°
        // Di bawah 30° → kemungkinan keypoints terlalu dekat/noisy → buang
        return if (angle >= 30.0) angle else null
    }

    /**
     * Menghitung sudut lutut kanan dari Person
     * Menggunakan keypoints: RIGHT_HIP, RIGHT_KNEE, RIGHT_ANKLE
     */
    fun getRightKneeAngle(person: Person): Double? {
        val hip = person.keypoints.getOrNull(BodyPart.RIGHT_HIP)
        val knee = person.keypoints.getOrNull(BodyPart.RIGHT_KNEE)
        val ankle = person.keypoints.getOrNull(BodyPart.RIGHT_ANKLE)

        if (hip == null || knee == null || ankle == null) return null
        if (hip.score < 0.2f || knee.score < 0.2f || ankle.score < 0.2f) return null

        val angle = calculateAngle(hip, knee, ankle)
        return if (angle >= 30.0) angle else null
    }

    /**
     * Menghitung sudut siku kiri dari Person
     * Menggunakan keypoints: LEFT_SHOULDER, LEFT_ELBOW, LEFT_WRIST
     */
    fun getLeftElbowAngle(person: Person): Double? {
        val shoulder = person.keypoints.getOrNull(BodyPart.LEFT_SHOULDER)
        val elbow = person.keypoints.getOrNull(BodyPart.LEFT_ELBOW)
        val wrist = person.keypoints.getOrNull(BodyPart.LEFT_WRIST)

        if (shoulder == null || elbow == null || wrist == null) return null
        if (shoulder.score < 0.2f || elbow.score < 0.2f || wrist.score < 0.2f) return null

        val angle = calculateAngle(shoulder, elbow, wrist)
        // Sudut siku valid: 20°–180°
        return if (angle >= 20.0) angle else null
    }

    /**
     * Menghitung sudut siku kanan dari Person
     * Menggunakan keypoints: RIGHT_SHOULDER, RIGHT_ELBOW, RIGHT_WRIST
     */
    fun getRightElbowAngle(person: Person): Double? {
        val shoulder = person.keypoints.getOrNull(BodyPart.RIGHT_SHOULDER)
        val elbow = person.keypoints.getOrNull(BodyPart.RIGHT_ELBOW)
        val wrist = person.keypoints.getOrNull(BodyPart.RIGHT_WRIST)

        if (shoulder == null || elbow == null || wrist == null) return null
        if (shoulder.score < 0.2f || elbow.score < 0.2f || wrist.score < 0.2f) return null

        val angle = calculateAngle(shoulder, elbow, wrist)
        return if (angle >= 20.0) angle else null
    }

    /**
     * Mendapatkan rata-rata sudut lutut (kiri dan kanan).
     *
     * Bila hasilnya null, log tiap frame WHY — keypoint mana yang gagal lolos threshold
     * confidence (0.2) dan berapa skornya. Kritikal untuk mendiagnosa kenapa squat
     * tidak terdeteksi: biasanya karena hip/knee/ankle punya skor di bawah threshold.
     */
    fun getAverageKneeAngle(person: Person): Double? {
        val leftAngle = getLeftKneeAngle(person)
        val rightAngle = getRightKneeAngle(person)

        val result = when {
            leftAngle != null && rightAngle != null -> (leftAngle + rightAngle) / 2
            leftAngle != null -> leftAngle
            rightAngle != null -> rightAngle
            else -> null
        }

        if (result == null) {
            val lh = person.keypoints.getOrNull(BodyPart.LEFT_HIP)
            val lk = person.keypoints.getOrNull(BodyPart.LEFT_KNEE)
            val la = person.keypoints.getOrNull(BodyPart.LEFT_ANKLE)
            val rh = person.keypoints.getOrNull(BodyPart.RIGHT_HIP)
            val rk = person.keypoints.getOrNull(BodyPart.RIGHT_KNEE)
            val ra = person.keypoints.getOrNull(BodyPart.RIGHT_ANKLE)
            Log.d(TAG, ("knee=null (threshold=0.20) | " +
                "L hip=%.2f knee=%.2f ank=%.2f | R hip=%.2f knee=%.2f ank=%.2f").format(
                lh?.score ?: -1f, lk?.score ?: -1f, la?.score ?: -1f,
                rh?.score ?: -1f, rk?.score ?: -1f, ra?.score ?: -1f))
        }

        return result
    }

    /**
     * Mendapatkan rata-rata sudut siku (kiri dan kanan)
     */
    fun getAverageElbowAngle(person: Person): Double? {
        val leftAngle = getLeftElbowAngle(person)
        val rightAngle = getRightElbowAngle(person)

        return when {
            leftAngle != null && rightAngle != null -> (leftAngle + rightAngle) / 2
            leftAngle != null -> leftAngle
            rightAngle != null -> rightAngle
            else -> null
        }
    }
}
