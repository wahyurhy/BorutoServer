package com.wahyurhy

import com.wahyurhy.models.ApiResponse
import com.wahyurhy.repository.HeroRepositoryImpl
import com.wahyurhy.repository.NEXT_PAGE_KEY
import com.wahyurhy.repository.PREVIOUS_PAGE_KEY
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    private val heroRepository = HeroRepositoryImpl()

    @Test
    fun `access root endpoint, assert correct information`() = testApplication {
        client.get("/").apply {
            assertEquals(
                expected = HttpStatusCode.OK,
                actual = status
            )
            assertEquals(
                expected = "Welcome to Boruto API!",
                actual = bodyAsText()
            )
        }
    }

    @Test
    fun `access all heroes endpoint, query non existing page number, assert error`() = testApplication {

        client.get("/boruto/heroes?page=6").apply {
            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = status
            )
            assertEquals(
                expected = "Page not Found.",
                actual = bodyAsText()
            )
        }
    }

    @Test
    fun `access all heroes endpoint, query invalid page number, assert error`() = testApplication {

        client.get("/boruto/heroes?page=invalid").apply {
            assertEquals(
                expected = HttpStatusCode.BadRequest,
                actual = status
            )
            val expected = ApiResponse(
                success = false,
                message = "Only Numbers Allowed."
            )
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText())
            assertEquals(
                expected = expected,
                actual = actual
            )
        }
    }

    @Test
    fun `access all heroes endpoint, query all pages, assert correct query information`() = testApplication {
        val pages = 1..5
        val heroes = listOf(
            heroRepository.page1,
            heroRepository.page2,
            heroRepository.page3,
            heroRepository.page4,
            heroRepository.page5
        )
        pages.forEach { page ->
            client.get("/boruto/heroes?page=$page").apply {
                println("CURRENT PAGE: $page")
                assertEquals(
                    expected = HttpStatusCode.OK,
                    actual = status
                )
                val expected = ApiResponse(
                    success = true,
                    message = "ok",
                    prevPage = calculatePage(page = page)["prevPage"],
                    nextPage =calculatePage(page = page)["nextPage"],
                    heroes = heroes[page - 1]
                )
                val actual = Json.decodeFromString<ApiResponse>(bodyAsText())
                println("PREV PAGE: ${calculatePage(page = page)["prevPage"]}")
                println("NEXT PAGE: ${calculatePage(page = page)["nextPage"]}")
                println("HEROES: ${heroes[page - 1]}")
                assertEquals(
                    expected = expected,
                    actual = actual
                )
            }
        }
    }

    private fun calculatePage(page: Int): Map<String, Int?> {
        var prevPage: Int? = page
        var nextPage: Int? = page
        if (page in 1..4) {
            nextPage = nextPage?.plus(1)
        }
        if (page in 2..5) {
            prevPage = prevPage?.minus(1)
        }
        if (page == 1) {
            prevPage = null
        }
        if (page == 5) {
            nextPage = null
        }
        return mapOf(PREVIOUS_PAGE_KEY to prevPage, NEXT_PAGE_KEY to nextPage)
    }

    @Test
    fun `access search heroes endpoint, query hero name, assert single hero result`() = testApplication {
        val name = "sasuke"
        client.get("/boruto/heroes/search?name=$name").apply {
            assertEquals(
                expected = HttpStatusCode.OK,
                actual = status
            )
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText())
                .heroes.size
            assertEquals(
                expected = 1,
                actual = actual
            )
        }
    }

    @Test
    fun `assert search heroes endpoint, query hero name, assert multiple heroes result`() = testApplication {
        val query = "sa"
        client.get("/boruto/heroes/search?name=$query").apply {
            assertEquals(
                expected = HttpStatusCode.OK,
                actual = status
            )
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText())
                .heroes.size
            assertEquals(
                expected = 3,
                actual = actual
            )
        }
    }

    @Test
    fun `assert search heroes endpoint, query an empty text, assert empty list as a result`() = testApplication {
        client.get("boruto/heroes/search?name=").apply {
            assertEquals(
                expected = HttpStatusCode.OK,
                actual = status
            )
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText())
                .heroes
            assertEquals(
                expected = emptyList(),
                actual = actual
            )
        }
    }

    @Test
    fun `assert search heroes endpoint, query non existing hero, assert empty list as a result`() = testApplication {
        client.get("boruto/heroes/search?name=unknown").apply {
            assertEquals(
                expected = HttpStatusCode.OK,
                actual = status
            )
            val actual = Json.decodeFromString<ApiResponse>(bodyAsText())
                .heroes
            assertEquals(
                expected = emptyList(),
                actual = actual
            )
        }
    }

    @Test
    fun `assert non existing endpoint, assert not found`() = testApplication {
        client.get("/unknown").apply {
            assertEquals(
                expected = HttpStatusCode.NotFound,
                actual = status
            )
            assertEquals(
                expected = "Page not Found.",
                actual = bodyAsText()
            )
        }
    }
}
