package net.blerg.concurrencyExperiment.kotlin
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlinx.coroutines.*
import org.apache.commons.lang3.math.NumberUtils

data class Pixel(val x:Int, val y:Int, val rgb:Int)

fun main(args: Array<String>) {
    val startTime = System.currentTimeMillis()
    val images = readImages()
    val hdr = createHdr(images)
    saveImage(hdr)
    val timeTaken = System.currentTimeMillis() - startTime
    println("Kotlin time taken: $timeTaken ms")
}

private fun readImages() = (0..3).mapNotNull {ImageIO.read(File("resources/${it+1}.JPG")) }

private fun createHdr(images: List<BufferedImage>):BufferedImage {
    val width = images[0].width
    val height = images[0].height

    val hdr = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    runBlocking {

        (0 until width).flatMap { x ->
            (0 until height).map { y ->
                val colours = HashSet<Int>(4)
                (0..3).forEach{colours.add(images[it].getRGB(x,y))}
                calculateColour(x, y, colours)
            }
        }
                .map { it.await() }
                .forEach { hdr.setRGB(it.x, it.y, it.rgb) }
    }

    return hdr
}

fun calculateColour(x: Int, y: Int, colours: Set<Int>) = GlobalScope.async {

        var largestNumber = 0
        var colourWithLargestNumber = colours.first()

        colours.forEach{

            val colour = Color(it)

            val red = colour.red
            val green = colour.green
            val blue = colour.blue

            val max = NumberUtils.max(red, green, blue)
            val min = NumberUtils.min(red, green, blue)

            val diff = max - min

            if (diff > largestNumber) {
                largestNumber = diff
                colourWithLargestNumber = colour.rgb
            }
        }

        Pixel(x, y, colourWithLargestNumber)
}

private fun saveImage(hdr: BufferedImage) {
    val hdrFile = File("kotlin-hdr.jpg")
    ImageIO.write(hdr, "jpg", hdrFile)
}