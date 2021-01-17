import org.w3c.dom.CanvasRenderingContext2D

class TestCanvas(val MYCTX: CanvasRenderingContext2D) : MyCanvas {
    override fun drawImage(b: dynamic, toDouble: Double, d: Double) {
        MYCTX.drawImage(b, toDouble, d)

    }

    override fun drawImage(b: dynamic, toDouble: Int, d: Int) {
        MYCTX.drawImage(b,toDouble,d)
    }

}