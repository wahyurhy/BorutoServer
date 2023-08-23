package com.wahyurhy.di

import com.wahyurhy.repository.HeroRepository
import com.wahyurhy.repository.HeroRepositoryImpl
import org.koin.dsl.module

val koinModule = module {
    single<HeroRepository> {
        HeroRepositoryImpl()
    }
}