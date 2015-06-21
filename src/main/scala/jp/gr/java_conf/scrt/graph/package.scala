package jp.gr.java_conf.scrt


import android.util.Log

package object graph {
  val tag = "Graph"
  def debug(msg: String): Unit = {
    Log.d(tag, msg)
  }
}

