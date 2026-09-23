package id.darno

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    val dotenv = dotenv {
        ignoreIfMissing = true
    }

    dotenv.entries().forEach {
        System.setProperty(it.key, it.value)
    }

    EngineMain.main(args)
}
