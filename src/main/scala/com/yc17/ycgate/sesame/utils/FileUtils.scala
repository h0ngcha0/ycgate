package com.yc17.ycgate.sesame.utils

import scala.util.{Try, Success, Failure}
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.nio.charset.Charset
import scala.reflect.{ClassTag, classTag}
import java.io.IOException

class FileUtils {
  def mkdir(path: Path): Unit = {
    Files.createDirectories(path)
  }

  def rmdir(path: Path): Unit = {
    recover[NoSuchFileException, Unit]({}) {
      Files.walkFileTree(path, new SimpleFileVisitor[Path]() {
        override def visitFile(
          file: Path,
          attrs: BasicFileAttributes): FileVisitResult = {
          Files.delete(file)
          FileVisitResult.CONTINUE
        }

        override def postVisitDirectory(
          dir: Path,
          exc: IOException): FileVisitResult = {
          Files.delete(dir)
          return FileVisitResult.CONTINUE;
        }
      })
    }
  }

  def writeToFile(path: Path, string: String, append: Boolean = false): Unit = {
    val option = (Files.exists(path), append) match {
      case (false, _)    => StandardOpenOption.CREATE
      case (true, true)  => StandardOpenOption.APPEND
      case (true, false) => StandardOpenOption.TRUNCATE_EXISTING
    }

    if (option == StandardOpenOption.CREATE) mkdir(path.getParent)
    Files.write(path, string.getBytes, option)
  }

  private def recover[T <: Exception: ClassTag, A](recoveredValue: A)(f: => A): A = {
    (Try { f } recover {
      case e: T => recoveredValue
      case e    => throw(e)
    }).get
  }

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }
}
