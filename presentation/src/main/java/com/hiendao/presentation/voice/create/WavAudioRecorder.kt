package com.hiendao.presentation.voice.create

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class WavAudioRecorder {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingThread: Thread? = null
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    @SuppressLint("MissingPermission")
    fun startRecording(outputFile: File) {
        if (isRecording) return

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        audioRecord?.startRecording()
        isRecording = true

        recordingThread = Thread {
            writeAudioDataToFile(outputFile)
        }
        recordingThread?.start()
    }

    fun stopRecording(outputFile: File) {
        if (!isRecording) return
        isRecording = false

        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        recordingThread = null

        // Convert raw PCM to WAV
        val rawFile = File(outputFile.parent, "temp_raw.pcm")
        if (rawFile.exists()) {
             try {
                rawToWave(rawFile, outputFile)
             } catch (e: Exception) {
                 e.printStackTrace()
             } finally {
                rawFile.delete()
             }
        }
    }

    private fun writeAudioDataToFile(outputFile: File) {
        val rawFile = File(outputFile.parent, "temp_raw.pcm")
        val data = ByteArray(bufferSize)
        val os = FileOutputStream(rawFile)

        try {
            while (isRecording) {
                val read = audioRecord?.read(data, 0, bufferSize) ?: 0
                if (read > 0) {
                    os.write(data, 0, read)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                os.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun rawToWave(rawFile: File, waveFile: File) {
        val rawData = ByteArray(rawFile.length().toInt())
        var input: FileInputStream? = null
        try {
            input = FileInputStream(rawFile)
            input.read(rawData)
        } finally {
            input?.close()
        }

        var output: FileOutputStream? = null
        try {
            output = FileOutputStream(waveFile)
            writeWavHeader(output, rawData.size.toLong())
            output.write(rawData)
        } finally {
            output?.close()
        }
    }

    private fun writeWavHeader(output: FileOutputStream, totalAudioLen: Long) {
        val totalDataLen = totalAudioLen + 36
        val longSampleRate = sampleRate.toLong()
        val channels = 1
        val byteRate = (sampleRate * channels * 16 / 8).toLong()

        val header = ByteArray(44)
        
        // CKID: RIFF
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        // CKSize
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        // WAVEID
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        // fmt ID
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        // fmt Size
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        // FormatTag (1 for PCM)
        header[20] = 1
        header[21] = 0
        // Channels
        header[22] = channels.toByte()
        header[23] = 0
        // SampleRate
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = ((longSampleRate shr 8) and 0xff).toByte()
        header[26] = ((longSampleRate shr 16) and 0xff).toByte()
        header[27] = ((longSampleRate shr 24) and 0xff).toByte()
        // ByteRate
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        // BlockAlign
        header[32] = (channels * 16 / 8).toByte()
        header[33] = 0
        // BitsPerSample
        header[34] = 16
        header[35] = 0
        // DataID
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        // DataSize
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        output.write(header, 0, 44)
    }
}
