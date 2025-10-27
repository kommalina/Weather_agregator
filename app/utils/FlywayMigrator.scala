package utils

import org.flywaydb.core.Flyway

object FlywayMigrator {
  def migrate(): Unit = {
    val flyway = Flyway.configure()
      .dataSource(
        "jdbc:postgresql://localhost:5432/weatherdb",
        "postgres",
        "password"
      )
      .locations("classpath:/db/migration")
      .schemas("public")
      .baselineOnMigrate(true)
      .load()

    flyway.migrate()
  }
}
