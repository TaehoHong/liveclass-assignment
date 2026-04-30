package com.futureschole.liveclass

import com.futureschole.liveclass.config.TestcontainersConfiguration
import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<LiveclassApplication>().with(TestcontainersConfiguration::class).run(*args)
}
