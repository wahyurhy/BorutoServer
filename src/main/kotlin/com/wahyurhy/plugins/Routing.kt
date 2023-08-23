package com.wahyurhy.plugins

import com.wahyurhy.routes.getAllHeroes
import com.wahyurhy.routes.root
import com.wahyurhy.routes.searchHeroes
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        root()
        getAllHeroes()
        searchHeroes()

        staticResources("/images", "images")
    }
}
