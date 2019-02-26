package modules

import com.google.inject.AbstractModule
import dao.{ActivityDao, ActivityDaoImpl}
import net.codingwell.scalaguice.ScalaModule

/**
  * Main guice module with common dependencies
  */
class MainModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    bind[ActivityDao].to[ActivityDaoImpl]
  }
}
