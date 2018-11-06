package com.mpc.scalats.core

import com.mpc.scalats.core.ScalaModel._
import org.scalatest._

import scala.reflect.runtime.universe._

/**
  * Created by Milosz on 06.12.2016.
  */
class ScalaParserSpec extends FlatSpec with Matchers {

  it should "parse case class with one primitive member" in {
    val parsed = ScalaParser.parseCaseClasses(List(TestTypes.TestClass1Type))
    val expected = CaseClass("TestClass1", List(CaseClassMember("name", StringRef)), List.empty)
    parsed should contain(expected)
  }

  it should "parse generic case class with one member" in {
    val parsed = ScalaParser.parseCaseClasses(List(TestTypes.TestClass2Type))
    val expected = CaseClass("TestClass2", List(CaseClassMember("name", TypeParamRef("T"))), List("T"))
    parsed should contain(expected)
  }

  it should "parse generic case class with one member list of type parameter" in {
    val parsed = ScalaParser.parseCaseClasses(List(TestTypes.TestClass3Type))
    val expected = CaseClass(
      "TestClass3",
      List(CaseClassMember("name", SeqRef(TypeParamRef("T")))),
      List("T")
    )
    parsed should contain(expected)
  }

  it should "parse generic case class with one optional member" in {
    val parsed = ScalaParser.parseCaseClasses(List(TestTypes.TestClass5Type))
    val expected = CaseClass(
      "TestClass5",
      List(CaseClassMember("name", OptionRef(TypeParamRef("T")))),
      List("T")
    )
    parsed should contain(expected)
  }

  it should "correctly detect involved types" in {
    val parsed = ScalaParser.parseCaseClasses(List(TestTypes.TestClass6Type))
    parsed should have length 6
  }

  it should "correctly handle either types" in {
    val parsed = ScalaParser.parseCaseClasses(List(TestTypes.TestClass7Type))
    val expected = CaseClass(
      "TestClass7",
      List(CaseClassMember("name", UnionRef(CaseClassRef("TestClass1", List()),CaseClassRef("TestClass1B", List())))),
      List("T")
    )
    parsed should contain(expected)
  }

  it should "parse AnyVal case class" in {
    val parsed = ScalaParser.parseCaseClasses(List(TestTypes.TestClass8Type))
    val expected = CaseClass(
      "TestClass8",
      List(CaseClassMember("value", StringRef)),
      List.empty,
      true
    )
    parsed should contain(expected)
  }

}

object TestTypes {

  implicit val mirror: Mirror = runtimeMirror(getClass.getClassLoader)
  val TestClass1Type: Type = typeFromClass(classOf[TestClass1])
  val TestClass3Type: Type = typeFromClass(classOf[TestClass3[_]])
  val TestClass4Type: Type = typeFromClass(classOf[TestClass4[_]])
  val TestClass2Type: Type = typeFromClass(classOf[TestClass2[_]])
  val TestClass5Type: Type = typeFromClass(classOf[TestClass5[_]])
  val TestClass6Type: Type = typeFromClass(classOf[TestClass6[_]])
  val TestClass7Type: Type = typeFromClass(classOf[TestClass7[_]])
  val TestClass8Type: Type = typeFromClass(classOf[TestClass8])


  private def typeFromClass[T](clazz: Class[T])(implicit runtimeMirror: Mirror): Type =
    runtimeMirror.classSymbol(clazz).toType

  case class TestClass1(name: String)

  case class TestClass1B(foo: String)

  case class TestClass2[T](name: T)

  case class TestClass3[T](name: List[T])

  case class TestClass4[T](name: TestClass3[T])

  case class TestClass5[T](name: Option[T])


  case class TestClass6[T](name: Option[TestClass5[List[Option[TestClass4[String]]]]], age: TestClass3[TestClass2[TestClass1]])

  case class TestClass7[T](name: Either[TestClass1, TestClass1B])

  case class TestClass8(value: String) extends AnyVal

}
