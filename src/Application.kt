package io.kraftsman

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    // 先連線到 H2
    Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )

    // 先把資料表建立起來
    transaction {
        SchemaUtils.create(Cities, Users)
    }

    // 先寫一些資料進 H2
    transaction {
        val stPete = City.new {
            name = "St. Petersburg"
        }

        val munich = City.new {
            name = "Munich"
        }

        User.new {
            name = "User A"
            city = stPete
            age = 5
        }

        User.new {
            name = "User B"
            city = stPete
            age = 27
        }

        User.new {
            name = "User C"
            city = munich
            age = 42
        }
    }

    routing {

        get("/") {
            call.respondText("Hello, world")
        }

    }
}

object Users : IntIdTable() {
    val name = varchar("name", 50).index()
    val city = reference("city", Cities)
    val age = integer("age")
}

object Cities: IntIdTable() {
    val name = varchar("name", 50)
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var name by Users.name
    var city by City referencedOn Users.city
    var age by Users.age
}

class City(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<City>(Cities)

    var name by Cities.name
    val users by User referrersOn Users.city
}
