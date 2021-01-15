import org.w3c.dom.CanvasRenderingContext2D

fun CanvasRenderingContext2D.fillText(text: dynamic, x: Double, y: Int) {
    fillText(text, x, y.toDouble())
}

fun CanvasRenderingContext2D.fillRect(x: Double, y: Double, w: Int, h: Int) {
    fillRect(x, y, w.toDouble(), h.toDouble())
}

fun CanvasRenderingContext2D.fillRect(x: Double, y: Int, w: Int, h: Int) {
    fillRect(x, y.toDouble(), w.toDouble(), h.toDouble())
}


fun CanvasRenderingContext2D.fillRect(x: Int, y: Int, w: Int, h: Int) {
    fillRect(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())
}

fun CanvasRenderingContext2D.drawImage(image: dynamic, dx: Int, dy: Int) {
    drawImage(image, dx.toDouble(), dy.toDouble())
}


