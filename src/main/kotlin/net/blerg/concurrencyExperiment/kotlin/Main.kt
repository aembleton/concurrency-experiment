package net.blerg.concurrencyExperiment.kotlin

import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    val startTime = System.currentTimeMillis()
    val images = readImages()

    val timeTaken = System.currentTimeMillis() - startTime
    println("Kotlin time taken: $timeTaken ms")
}

private fun readImages() = (0..3).map {ImageIO.read(File("resources/${it+1}.JPG")) }