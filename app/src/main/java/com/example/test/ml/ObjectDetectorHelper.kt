package com.example.test.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import org.tensorflow.lite.support.image.TensorImage

class ObjectDetectorHelper(context: Context) {

    private val detector: ObjectDetector

    init {

        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setScoreThreshold(0.5f)
            .setMaxResults(5)
            .build()

        detector = ObjectDetector.createFromFileAndOptions(
            context,
            "model.tflite",
            options
        )
    }

    fun detect(bitmap: Bitmap) = detector.detect(TensorImage.fromBitmap(bitmap))
}