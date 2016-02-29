package org.scalacourse

import com.synergygb.zordon.common.data.DataTransformContext
import com.synergygb.zordon.core.ServiceBoot
import com.synergygb.zordon.gen.models.{student, grade, error}
import com.synergygb.zordon.gen.routes.ApplicationRoutesConsolidated
import spray.json.DefaultJsonProtocol
import spray.routing.Route
import spray.http.StatusCodes._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by saul on 1/14/16.
  */
object Boot extends App with ServiceBoot with ApplicationRoutesConsolidated with DefaultJsonProtocol {

  import Context.keyValueStore

  protected def apiResourceClass = getClass

  override def dataContext: DataTransformContext = DataContext

  def preload(): Unit ={
    val students = Seq(student(
      name = "Francis",
      ci = "12345678",
      grades = Option(Seq(grade(
        id = "1",
        date = "2014-05-04",
        signature = "Programming",
        grade = 20
      ), grade(
        id = "2",
        date = "2014-05-05",
        signature = "Guitar",
        grade = 20
      )))
    ), student(
      name = "Ajax",
      ci = "23456789",
      grades = Option(Seq(grade(
        id = "1",
        date = "2014-06-06",
        signature = "Arts",
        grade = 17
      ), grade(
        id = "2",
        date = "2014-07-07",
        signature = "Trolling 101",
        grade = 15
      )))
    ))
    onSuccess(keyValueStore.write("students", students))
  }

  override def handleGetStudent(date: Option[String], signature: Option[String], grade: Option[Double])(): Route = {
    preload
    onSuccess(keyValueStore.read[Seq[student]]("students")) {
      x => {
        val students = x.map(y => y).getOrElse(Seq())

        complete(OK, students)
      }
    }
  }

  override def handlePostStudent(myStudent: student)(): Route = {
    preload
    onSuccess(keyValueStore.read[Seq[student]]("students")) {
      x => {
        val students = List(x.map(studnt => studnt).getOrElse(Seq[student]()))

        students :+ myStudent

        onSuccess(keyValueStore.write("students", students)) {x =>
          complete(Created, myStudent)
        }
      }
    }
  }

  override def handleGetStudentStudentId(studentId: String, date: Option[String], signature: Option[String], grade: Option[Double])(): Route = {
    preload
    onSuccess(keyValueStore.read[student](studentId)) { x =>
       x.map(searchedStudent => complete(searchedStudent)).getOrElse(complete(error(
         msg = "Student was not found",
         code = "1204"
       )))
    }
  }

  override def handleDeleteStudentStudentId(studentId: String)(): Route = {
    preload
    onSuccess(keyValueStore.read[student](studentId)) { studentToDelete =>

      keyValueStore.delete(studentId)

      studentToDelete.map(searchedStudent => complete(searchedStudent)).getOrElse(complete(error(
        msg = "Student was not found",
        code = "1204"
      )))
    }
  }

  override def handlePutStudentStudentId(studentId: String, name: Option[student])(): Route = {
    preload
    onSuccess(keyValueStore.read[student](studentId)) { studentToModify =>

      studentToModify.map(x => name.map(y => complete(OK, y)).getOrElse(complete(OK))).getOrElse(complete(error(
        msg = "Student was not found",
        code = "1204"
      )))
    }
  }

  override def handleDeleteStudentStudentIdGradesGradeId(studentId: String, gradeId: String)(): Route = {
    preload
    onSuccess(keyValueStore.read[student](studentId)) { studentGot =>

      val resp = for {
        myStudent <- studentGot
        myGrades <- myStudent.grades
        myStudent.grades = Option(myGrades.filter(_.id != gradeId))
      } yield complete(myStudent)

      resp.getOrElse(complete(error(
        msg = "Student got no notes",
        code = "1313"
      )))
    }

  }

  override def handlePostStudentStudentIdGrades(studentId: String, grades: grade)(): Route = {
    preload
    onSuccess(keyValueStore.read[student](studentId)) { studentGot =>
      val result = for {
        myStudent <- studentGot
        note <- myStudent.grades
        myStudent.grades = Option(note :+ Option(grades))
      } yield complete(myStudent)

      result.getOrElse(complete(error(
        msg = "Student was not found",
        code = "1204"
      )))
    }
  }

  override def handleGetStudentStudentIdGrades(studentId: String, date: Option[String], signature: Option[String], grade: Option[Double])(): Route = {
    preload
    onSuccess(keyValueStore.read[student](studentId)) { studentGot =>
      studentGot.map(x =>
        complete(x.grades)
      ).getOrElse(
        complete(error(
        msg = "Student was not found",
        code = "1204"
      )))
    }
  }

  override def handleGetStudentStudentIdGradesGradeId(studentId: String, gradeId: String, date: Option[String])(): Route = {
    preload
    onSuccess(keyValueStore.read[student](studentId)) { studentGot =>
      val result = for {
        student <- studentGot
        note <- student.grades
        rightGrade <- note.find(_.id == gradeId)
      } yield complete(rightGrade)

      result.getOrElse(complete(error(
        msg = "Student was not found",
        code = "1204"
      )))
    }
  }
}
