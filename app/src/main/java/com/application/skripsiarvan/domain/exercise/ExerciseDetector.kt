package com.application.skripsiarvan.domain.exercise

import com.application.skripsiarvan.domain.model.ExerciseState
import com.application.skripsiarvan.domain.model.Person

/**
 * Interface untuk deteksi gerakan latihan
 * Sesuai dengan Flowchart 4.4 Perhitungan Repetisi di skripsi
 */
interface ExerciseDetector {
    
    /**
     * Menganalisis frame dan mendeteksi state gerakan
     * @param person Hasil deteksi pose
     * @return State gerakan saat ini
     */
    fun analyzeFrame(person: Person?): ExerciseState
    
    /**
     * Mendapatkan jumlah repetisi yang terdeteksi
     */
    fun getRepetitionCount(): Int
    
    /**
     * Mendapatkan sudut sendi utama yang sedang dipantau
     * @return Sudut dalam derajat, null jika tidak terdeteksi
     */
    fun getCurrentAngle(): Double?
    
    /**
     * Reset counter dan state
     */
    fun reset()
}
