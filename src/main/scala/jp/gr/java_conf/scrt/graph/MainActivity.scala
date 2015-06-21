package jp.gr.java_conf.scrt.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.SurfaceView
import android.view.SurfaceHolder
import android.view.MotionEvent
import android.view.Window
import android.view.GestureDetector
import android.view.ScaleGestureDetector
import java.util.Calendar

class MainActivity extends TypedActivity {
  lazy val surface = new MySurface(this)

  lazy val detector = new GestureDetector(this, surface)

  /** Called when the activity is first created. */
  override def  onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    requestWindowFeature(Window.FEATURE_NO_TITLE)

    setContentView(R.layout.main)


    val body = findView(TR.body2)
    body.addView(surface, 0)
    // setContentView(surface)

  }

  override def onTouchEvent(me: MotionEvent): Boolean = {
    detector.onTouchEvent(me)

    // debug("surface:" + surface)
    // debug("scaleDetector:" + surface.scaleDetector)
    surface.scaleDetector.onTouchEvent(me)
    super.onTouchEvent(me)
  }

  override def  onResume() {
    super.onResume()
    debug("onResume")
  }

  override def onPause() {
    super.onPause()
    debug("onPause")
  }


}


import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class MySurface(c: Context) extends SurfaceView(c)
  with SurfaceHolder.Callback
  with GestureDetector.OnGestureListener
  with GestureDetector.OnDoubleTapListener { self =>

  debug("MySurface created.")

  private var scale = 1.0f

  val sdl: ScaleGestureDetector.SimpleOnScaleGestureListener =
    new ScaleGestureDetector.SimpleOnScaleGestureListener {

    override def onScale(d: ScaleGestureDetector): Boolean = {
      val f = scaleDetector.getScaleFactor
      if (f < 1.0f) {
        scale *= 0.99f
        if (scale < 0.5f) scale = 0.5f
      }
      else {
        scale *= 1.01f
        if (scale > 2.0f) scale = 2.0f
      }
      debug("onScale scale:" + scale)
      super.onScale(d)
    }

    override def onScaleBegin(d: ScaleGestureDetector): Boolean = {
      debug("onScaleBegin")
      super.onScaleBegin(d)
    }

    override def onScaleEnd(d: ScaleGestureDetector): Unit = {
      debug("onScaleEnd")
      super.onScaleEnd(d)
    }
  }
  val scaleDetector = new ScaleGestureDetector(c, sdl)

  override def onDoubleTap(me: MotionEvent): Boolean = {
    scale = 1.0f
    topLeft = (0.0f, 0.0f)
    debug("onDoubleTap")
    true
  }

  override def onDoubleTapEvent(me: MotionEvent): Boolean = {
    debug("onDoubleTapEvent")
    true
  }

  override def onSingleTapConfirmed(me: MotionEvent): Boolean = {
    debug("onSingleTapConfirmed")
    true
  }

  override def onDown(me: MotionEvent): Boolean = {
    // debug("onDown")
    true
  }

  override def onFling(me: MotionEvent, me2: MotionEvent, f1: Float, f2: Float): Boolean = {
    // debug("onFling")
    true
  }

  override def onLongPress(me: MotionEvent): Unit = {
    // debug("onLongPress")
  }

  override def onShowPress(me: MotionEvent): Unit = {
    // debug("onShowPress")
  }
  override def onScroll(me: MotionEvent, me2: MotionEvent, f1: Float, f2: Float): Boolean = {
    // debug("onScroll")
    topLeft = (topLeft._1 + f1 * -1, topLeft._2 + f2 * -1)
    true
  }

  override def onSingleTapUp(me: MotionEvent): Boolean = {
    // debug("onSingleTapUp")
    true
  }



  getHolder.addCallback(this)

  private var sh: Option[SurfaceHolder] = None

  lazy val f = Future {
    // debug("future start")

    @scala.annotation.tailrec
    def update(sh: Option[SurfaceHolder]) {
      sh match {
        case Some(h) => {
          val canvas = h.lockCanvas(null)
          try {
            self.onDraw(canvas)
            // Thread.sleep(32)
          }
          finally {
            h.unlockCanvasAndPost(canvas)
          }
          update(sh)
        }
        case None => debug("update end")
      }
    }

    update(sh)
  }

  def isCompleted = f.isCompleted

  override def surfaceChanged(sh: SurfaceHolder, i1: Int, i2: Int, i3: Int): Unit = {
    this.sh = Option(sh)
    debug("surfaceChanged")
  }

  override def surfaceCreated(sh: SurfaceHolder): Unit = {
    this.sh = Option(sh)
    debug("surfaceCreated. f is completed. " + f.isCompleted)
  }

  override def surfaceDestroyed(sh: SurfaceHolder): Unit = {
    this.sh = null
    debug("surfaceDestroyed. f is completed. " + f.isCompleted)
  }

  // 設定
  val step = 50.0f
  val border = Color.argb(255, 120, 120, 120)
  val backgrounds =
    Array(Color.argb(0xff, 0xff, 0xf3, 0xe7), Color.argb(0xff, 0xff, 0xe9, 0xd7))

  val background = new Paint

  val text = new Paint
  text.setColor(Color.argb(255, 120, 120, 120))
  text.setTextSize(10.0f)
  text.setAntiAlias(true)
  text.setTypeface(Typeface.DEFAULT_BOLD)

  val text2 = new Paint
  text2.setColor(Color.argb(255, 120, 120, 120))
  text2.setTextSize(10.0f)
  text2.setAntiAlias(true)
  text2.setTypeface(Typeface.DEFAULT_BOLD)

  val dashLine = new Paint
  dashLine.setColor(border)
  dashLine.setPathEffect(new DashPathEffect(Array(5.0f, 3.0f), 0))
  dashLine.setStyle(Paint.Style.STROKE)
  dashLine.setStrokeWidth(1.0f)

  private var num = 0
  private var topLeft = (0.0f, 0.0f)
  def getTopLeft(): (Float, Float) = {
    if (num % 1000 == 0) debug("topLeft:" + topLeft)
    num += 1
    topLeft
  }

  override def onDraw(canvas: Canvas) {
    val stepNow: Float = step * scale

    val topLeft = getTopLeft()

    // 背景を塗りつぶし
    canvas.drawColor(Color.WHITE)

    var index = 0
    var date = Calendar.getInstance

    @scala.annotation.tailrec
    def move(x: Float): Float = {
      if (x > 0.0f) {
        index += 1
        date.add(Calendar.DAY_OF_MONTH, -1)
        move(x - stepNow)
      }
      else if (x < stepNow * -1) {
        index += 1
        date.add(Calendar.DAY_OF_MONTH, 1)
        move(x + stepNow)
      }
      else {
        x
      }
    }

    var x = move(topLeft._1)

    // 縦に色分けする
    for (i <- 0 until (canvas.getWidth / stepNow.toInt) + 1) {
      val start = x + i * stepNow
      val end = start + stepNow

      date.add(Calendar.DAY_OF_MONTH, 1)
      val ds = s"${date.get(Calendar.MONTH)}/${date.get(Calendar.DAY_OF_MONTH)}"

      background.setColor(backgrounds(index % 2))
      canvas.drawRect(start, 0, end, canvas.getHeight, background)
      canvas.drawText(ds, 0, ds.length, start + 15.0f, canvas.getHeight - 25.0f, text2)
      index += 1
    }


    // 横の点線を描画
    var base = 50.0
    @scala.annotation.tailrec
    def moveY(y: Float): Float = {
      if (y > 0.0f) {
        base += 0.1f
        moveY(y - stepNow)
      }
      else if (y < stepNow * -1) {
        base -= 0.1f
        moveY(y + stepNow)
      }
      else {
        y
      }
    }

    val y = moveY(topLeft._2)


    for (i <- 0 until (canvas.getHeight / stepNow.toInt) + 1) {
      val positionY = y + i * stepNow
      val weight = base - i * 0.1
      val ws = f"$weight%3.2f"

      canvas.drawText(ws, 0, ws.length, 5.0f, positionY - 5.0f, text)
      canvas.drawLine(0, positionY, canvas.getWidth, positionY, dashLine)
    }


    // 折れ線グラフを描画する
    val point0 = (0.0f, 0.0f)
    val point1 = (50.0f * stepNow / step, 100.0f * stepNow / step)
    val point2 = (150.0f * stepNow / step, 150.0f * stepNow / step)
    val point3 = (200.0f * stepNow / step, 200.0f * stepNow / step)
    val point4 = (250.0f * stepNow / step, 100.0f * stepNow / step)

    drawGraph(Seq(point0, point1, point2, point3, point4), canvas, topLeft)
  }

  def drawGraph(pointSeq: Seq[(Float, Float)], canvas: Canvas, topLeft: (Float, Float)): Unit = {
    val points = (for (p <- pointSeq) yield
      Seq(p._1 + topLeft._1, p._2 + topLeft._2, p._1 + topLeft._1, p._2 + topLeft._2)).flatten.
        tail.tail.init.init


    canvas.drawLines(points.toArray, paint)

    val white = new Paint
    white.setColor(Color.argb(0xff, 0xff, 0xff, 0xff))
    white.setAntiAlias(true)
    for (p <- pointSeq) {
      var radius = 5.5f
      canvas.drawCircle(p._1 + topLeft._1, p._2 + topLeft._2, radius, paint)

      radius *= 0.75f
      canvas.drawCircle(p._1 + topLeft._1, p._2 + topLeft._2, radius, white)
    }
  }

  val paint = new Paint
  paint.setStrokeWidth(3)
  paint.setAntiAlias(true)
  paint.setColor(Color.argb(255, 0xf5, 0x79, 0x79))

}
