package lLSBot.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@SpringBootApplication
class BotApplication

fun main(args: Array<String>) {
    val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
    botsApi.registerBot(TelegramLLSBot())
    runApplication<BotApplication>(*args)
}