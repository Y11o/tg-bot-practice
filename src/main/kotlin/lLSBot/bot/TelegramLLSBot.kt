package lLSBot.bot

import com.google.gson.Gson
import lLSBot.groupsID.GroupID
import org.jvnet.hk2.annotations.Service
import org.springframework.beans.factory.annotation.Value
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import java.lang.NumberFormatException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.Calendar.*

var userGroup = 0
var fullGroupNumbers: List<String> = listOf()
var groupId: List<Long> = listOf()
val calendarInstance: Calendar = getInstance()
var weekNum = 0

@Service
class TelegramLLSBot : TelegramLongPollingBot() {

    @Value("\${telegram.token}")
    private val token: String = "5895349514:AAHh30bJUHvIM4Y11FtWIsjSjdrm7A60soY"

    @Value("\${telegram.botName}")
    private val botName: String = "LETI Lesson Schedule"

    override fun getBotToken(): String = token

    override fun getBotUsername(): String = botName

    override fun onUpdateReceived(update: Update?) {
        if (update!!.hasMessage()) {
            val message = update.message
            val chatId = message.chatId
            val responseText = if (message.hasText()) {
                val messageText = message.text
                if (weekNum == 0){
                    setWeekNum()
                }
                when {
                    messageText == "/start" -> "Добро пожаловать! Введите /help для получения справки или введите номер группы"
                    messageText == "/help" -> "Этот бот позволяет узнать расписание для вашей группы в ЛЭТИ! \n Введите номер вашей группы, чтобы продолжить"
                    messageText == "Ближайшее занятие" && (userGroup.toString() in fullGroupNumbers) -> nearLesson(
                        userGroup.toString()
                    )

                    messageText == "Расписание занятий в..." && (userGroup.toString() in fullGroupNumbers) -> dayOfWeek(
                        userGroup.toString()
                    )

                    messageText == "Расписание на следующий учебный день" && (userGroup.toString() in fullGroupNumbers) -> tomorrowSchedule(
                        userGroup.toString()
                    )

                    messageText == "Расписание на эту неделю" && (userGroup.toString() in fullGroupNumbers) -> weekSchedule(
                        userGroup.toString()
                    )

                    else -> "Вы написали: *$messageText*"
                }
            } else {
                "Я понимаю только текст"
            }
            sendNotification(chatId, responseText)
        }
    }

    private fun setWeekNum() {
        if ((calendarInstance.get(WEEK_OF_YEAR) % 2 == 0 && ((calendarInstance.get(DAY_OF_YEAR) % 7) in 3..6)) ||
            (calendarInstance.get(WEEK_OF_YEAR) % 2 == 1 && ((calendarInstance.get(DAY_OF_YEAR) % 7) in 0..2))
        ) {
            weekNum = 1
        }
        if ((calendarInstance.get(WEEK_OF_YEAR) % 2 == 1 && ((calendarInstance.get(DAY_OF_YEAR) % 7) in 3..6)) ||
            (calendarInstance.get(WEEK_OF_YEAR) % 2 == 0 && ((calendarInstance.get(DAY_OF_YEAR) % 7) in 0..2))
        ) {
            weekNum = 2
        }
    }

    private fun getDay(): WeekDay {
        when (calendarInstance.get(DAY_OF_WEEK)) {
            MONDAY -> return WeekDay.Mon
            TUESDAY -> return WeekDay.Tue
            WEDNESDAY -> return WeekDay.Wed
            THURSDAY -> return WeekDay.Thu
            FRIDAY -> return WeekDay.Fri
            SATURDAY -> return WeekDay.Sat
            SUNDAY -> return WeekDay.Sun
        }
        return WeekDay.Sun
    }

    private fun getTime(): String {
        val hour = calendarInstance.get(HOUR)
        val minute = calendarInstance.get(MINUTE)
        return "${hour}:${minute}"
    }

    private fun parseSchedule(messageText: String): String {
        val userGroupID = groupId[fullGroupNumbers.indexOf(messageText)]
        val connectToSchedule =
            URL("https://digital.etu.ru/api/schedule/objects/publicated?groups=${userGroupID}&withSubjectCode=true&withURL=true").openConnection() as HttpURLConnection
        return connectToSchedule.inputStream.bufferedReader().readText()
    }

    private fun weekSchedule(messageText: String): String {
        val schedule = ""
        val notParsedSchedule = parseSchedule(messageText)


        return schedule
    }

    private fun tomorrowSchedule(messageText: String): String {
        val schedule = ""
        val notParsedSchedule = parseSchedule(messageText)

        return schedule
    }

    private fun dayOfWeek(messageText: String): String {
        val schedule = ""
        val notParsedSchedule = parseSchedule(messageText)

        return schedule
    }

    private fun nearLesson(messageText: String): String {
        val schedule = ""
        val notParsedSchedule = parseSchedule(messageText)
        val currDay = getDay()
        val currTime = getTime().toLong().toString() //???????????????????????????



        return schedule
    }

    private fun sendNotification(chatId: Long, responseText: String) {
        val responseMessage = SendMessage(chatId.toString(), responseText)
        responseMessage.enableMarkdown(true)
        try {
            if (responseText.toInt() in 1000..9999 || (responseText[0] == '0' && responseText.length == 4)) {
                val connectToGroup =
                    URL("https://digital.etu.ru/api/general/dicts/groups?scheduleId=publicated&withFaculty=true&withSemesterSeasons=true&withFlows=true").openConnection() as HttpURLConnection
                val notParsedGroupIDs = connectToGroup.inputStream.bufferedReader().readText()
                fullGroupNumbers = listOf(Gson().fromJson(notParsedGroupIDs, GroupID::class.java).fullNumber)
                groupId = listOf(Gson().fromJson(notParsedGroupIDs, GroupID::class.java).id)
                if (responseText in fullGroupNumbers) {
                    userGroup = responseText.toInt()
                    responseMessage.replyMarkup = getReplyMarkup(
                        listOf(
                            listOf("Ближайшее занятие", "Расписание занятий в..."),
                            listOf("Расписание на следующий учебный день", "Расписание на эту неделю")
                        )
                    )
                }
            }
        } catch (nfe: NumberFormatException) {
            //not a valid int
        }
        execute(responseMessage)
    }

    private fun getReplyMarkup(allButtons: List<List<String>>): ReplyKeyboardMarkup {
        val markup = ReplyKeyboardMarkup()
        markup.keyboard = allButtons.map { rowButtons ->
            val row = KeyboardRow()
            rowButtons.forEach { rowButton -> row.add(rowButton) }
            row
        }
        return markup
    }

    enum class WeekDay(val value: String) {
        Fri("FRI"),
        Mon("MON"),
        Thu("THU"),
        Tue("TUE"),
        Wed("WED"),
        Sat("SAT"),
        Sun("SUN");

        companion object {
            public fun fromValue(value: String): WeekDay = when (value) {
                "FRI" -> Fri
                "MON" -> Mon
                "THU" -> Thu
                "TUE" -> Tue
                "WED" -> Wed
                "SAT" -> Sat
                "SUN" -> Sun
                else -> throw IllegalArgumentException()
            }
        }
    }
}