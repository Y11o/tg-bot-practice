package lLSBot.bot

import com.google.gson.Gson
import lLSBot.groupSchedule.GroupSchedule
import lLSBot.groupSchedule.ScheduleObject
import lLSBot.groupsID.GroupID
import org.jvnet.hk2.annotations.Service
import org.springframework.beans.factory.annotation.Value

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDateTime
import java.util.*
import java.util.Calendar.*



@Service


open class TelegramLLSBot : TelegramLongPollingBot() {

    @Value("\${telegram.token}")
    private val token: String = "5895349514:AAHh30bJUHvIM4Y11FtWIsjSjdrm7A60soY"

    @Value("\${telegram.botName}")
    private val botName: String = "LETI Lesson Schedule"

    override fun getBotToken(): String = token

    override fun getBotUsername(): String = botName

    private val groupChecker = "0301"
    private var prepareCheck = false
    protected open val mapOfUsersGroups: MutableMap<String, String> = mutableMapOf()
    private val fullGroupNumbers: MutableList<String> = mutableListOf()
    private val groupId: MutableList<Long> = mutableListOf()
    private val calendarInstance: Calendar = getInstance()
    private var weekNum = 0
    private var groupsFilled = false
    protected open var chatId: Long = 0

    override fun onUpdateReceived(update: Update?) {
        if (update!!.hasMessage()) {
            val message = update.message
            chatId = message.chatId
            val defaultDays = listOf("понедельник", "пн", "вторник", "вт", "среда", "ср", "четверг", "чт", "пятница", "пт", "суббота", "сб")
            val days = listOf("понедельник 1", "пн 1", "вторник 1", "вт 1", "среда 1", "ср 1", "четверг 1", "чт 1", "пятница 1", "пт 1", "суббота 1", "сб 1",
                              "понедельник 2", "пн 2", "вторник 2", "вт 2", "среда 2", "ср 2", "четверг 2", "чт 2", "пятница 2", "пт 2", "суббота 2", "сб 2",
                              "понедельник", "пн", "вторник", "вт", "среда", "ср", "четверг", "чт", "пятница", "пт", "суббота", "сб")
            if (message.text.lowercase() in days && prepareCheck) {
                if (message.text.lowercase() in defaultDays){
                    message.text += " $weekNum"
                }
                val responseText: String = dayOfWeek(mapOfUsersGroups[chatId.toString()].toString(),
                        message.text.lowercase().replaceFirst(".$".toRegex(), "").replaceFirst(".$".toRegex(), ""),
                        message.text.lowercase().last().digitToInt())
                prepareCheck = false
                sendNotification(chatId, responseText, update)
            }else{
                val responseText = if (message.hasText()) {
                    val messageText = message.text
                    if (weekNum == 0) {
                        setWeekNum()
                    }
                    when {
                        messageText == "/start" -> "Добро пожаловать! Введите /help для получения справки или введите номер группы"
                        messageText == "/help" -> "Этот бот позволяет узнать расписание для вашей группы в ЛЭТИ! \nВведите номер вашей группы, чтобы продолжить. Пример номера группы: $groupChecker"
                        messageText.startsWith("Ближайшее занятие") && (mapOfUsersGroups[chatId.toString()] in fullGroupNumbers) -> nearLesson(
                            mapOfUsersGroups[chatId.toString()].toString()
                        )
                        messageText.startsWith("Расписание занятий в...") && (mapOfUsersGroups[chatId.toString()] in fullGroupNumbers) -> dayPrepare()
                        messageText.startsWith("Расписание на следующий учебный день") && (mapOfUsersGroups[chatId.toString()] in fullGroupNumbers) -> tomorrowSchedule(
                            mapOfUsersGroups[chatId.toString()].toString()
                        )

                        messageText.startsWith("Расписание на эту неделю") && (mapOfUsersGroups[chatId.toString()] in fullGroupNumbers) -> weekSchedule(
                            mapOfUsersGroups[chatId.toString()].toString()
                        )
                        messageText.length == 4 -> messageText
                        else -> "Вы написали: *$messageText* - это база"
                    }
                } else {
                    "Я понимаю только текст"
                }
                sendNotification(chatId, responseText, update)
            }
        }
    }

    private fun dayPrepare(): String {
        prepareCheck = true
        return "Введите день недели. Пример: 'понедельник' или 'пн'.\n" +
                "Если хотите узнать расписание в этот день с учетом четности недели, введите её после дня недели. Пример: пн 2.\n" +
                "В воскресенье нет пар!"
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

    private fun getDay(tomorrowIndicator: Boolean): String {
        if (tomorrowIndicator){
            when (calendarInstance.get(DAY_OF_WEEK)) {
                MONDAY -> return "TUE"
                TUESDAY -> return "WED"
                WEDNESDAY -> return "THU"
                THURSDAY -> return "FRI"
                FRIDAY -> return "SAT"
                SATURDAY -> return "SUN"
                SUNDAY -> return "MON"
            }
        }else{
            when (calendarInstance.get(DAY_OF_WEEK)) {
                MONDAY -> return "MON"
                TUESDAY -> return "TUE"
                WEDNESDAY -> return "WED"
                THURSDAY -> return "THU"
                FRIDAY -> return "FRI"
                SATURDAY -> return "SAT"
                SUNDAY -> return "SUN"
            }
        }
        return "SUN"
    }

    private fun getTime(): String {
        val hour = calendarInstance.get(HOUR_OF_DAY)
        return "$hour"
    }

    private fun parseSchedule(userGroup: String): List<ScheduleObject> {
        val userGroupID = groupId[fullGroupNumbers.indexOf(userGroup)]
        val connectToSchedule =
            URL("https://digital.etu.ru/api/schedule/objects/publicated?groups=${URLEncoder.encode(userGroupID.toString(), "UTF-8")}&withSubjectCode=true&withURL=true").openConnection() as HttpURLConnection
        val notParsedSchedule = connectToSchedule.inputStream.bufferedReader().readText()
        val groupFullSchedule = Gson().fromJson(notParsedSchedule, Array<GroupSchedule>::class.java).toList()
        return groupFullSchedule[0].scheduleObjects
    }

    private fun weekSchedule(userGroup: String): String {
        var schedule = ""
        val weekSchedule = parseSchedule(userGroup)
        val thisWeekSchedule = weekSchedule.filter { it.lesson.auditoriumReservation.reservationTime.week.toInt() == weekNum }
        val sortedList = thisWeekSchedule.sortedWith(compareBy{ it.lesson.auditoriumReservation.reservationTime.startTime })
        for (day in listOf("MON","TUE","WED","THU","FRI","SAT")){
            for (seObject in sortedList.filter { it.lesson.auditoriumReservation.reservationTime.weekDay == day }){
                schedule += "День недели: ${seObject.lesson.auditoriumReservation.reservationTime.weekDay} \n" +
                        "Название предмета: ${seObject.lesson.subject.title} (${seObject.lesson.subject.subjectType}).\n" +
                        "В ${codeToStringTime(seObject.lesson.auditoriumReservation.reservationTime.startTime)} \n" +
                        "Аудитория: ${seObject.lesson.auditoriumReservation.auditorium?.number?: "-"}\n" +
                        "Преподаватель: ${seObject.lesson.teacher?.initials?: "-"}\n"
            }
        }
        if (schedule == ""){
            schedule = "На этой неделе нет пар!"
        }
        return schedule
    }

    private fun tomorrowSchedule(userGroup: String): String {
        var schedule = ""
        val tomorrowSchedule = parseSchedule(userGroup)
        val tomorrowDay: String = getDay(true)
        val tomorrowFilteredSchedule: List<ScheduleObject> = if (tomorrowDay != "SUN" && tomorrowDay != "MON"){
            tomorrowSchedule.filter { it.lesson.auditoriumReservation.reservationTime.week.toInt() == weekNum &&
                    it.lesson.auditoriumReservation.reservationTime.weekDay == tomorrowDay}
        }else{
            tomorrowSchedule.filter { it.lesson.auditoriumReservation.reservationTime.week.toInt() != weekNum &&
                    it.lesson.auditoriumReservation.reservationTime.weekDay == "MON" }
        }
        for (seObject in tomorrowFilteredSchedule.sortedWith(compareBy { it.lesson.auditoriumReservation.reservationTime.startTime })){
            schedule += "День недели: ${seObject.lesson.auditoriumReservation.reservationTime.weekDay} \n" +
                    "Название предмета: ${seObject.lesson.subject.title} (${seObject.lesson.subject.subjectType}).\n" +
                    "В ${codeToStringTime(seObject.lesson.auditoriumReservation.reservationTime.startTime)} \n" +
                    "Аудитория: ${seObject.lesson.auditoriumReservation.auditorium?.number?: "-"}\n" +
                    "Преподаватель: ${seObject.lesson.teacher?.initials?: "-"}\n"
        }
        if (schedule == ""){
            schedule = "Завтра нет пар!"
        }
        return schedule
    }

    private fun dayOfWeek(userGroup: String, userDay: String, userWeek: Int): String {
        var schedule = ""
        val userWeekDay = when (userDay) {
            "понедельник" -> "MON"
            "пн" -> "MON"
            "вторник" -> "TUE"
            "вт" -> "TUE"
            "среда" -> "WED"
            "ср" -> "WED"
            "четверг" -> "THU"
            "чт" -> "THU"
            "пятница" -> "FRI"
            "пт" -> "FRI"
            "суббота" -> "SAT"
            "сб" -> "SAT"
            else -> "SUN"
        }
        val parsedSchedule = parseSchedule(userGroup)
        val daySchedule = parsedSchedule.filter {
            it.lesson.auditoriumReservation.reservationTime.week.toInt() == userWeek &&
                    it.lesson.auditoriumReservation.reservationTime.weekDay == userWeekDay
        }
        for (seObject in daySchedule.sortedWith(compareBy { it.lesson.auditoriumReservation.reservationTime.startTime })) {
            schedule += "День недели: ${seObject.lesson.auditoriumReservation.reservationTime.weekDay} \n" +
                    "Название предмета: ${seObject.lesson.subject.title} (${seObject.lesson.subject.subjectType}).\n" +
                    "В ${codeToStringTime(seObject.lesson.auditoriumReservation.reservationTime.startTime)} \n" +
                    "Аудитория: ${seObject.lesson.auditoriumReservation.auditorium?.number?: "-"}\n" +
                    "Преподаватель: ${seObject.lesson.teacher?.initials?: "-"}\n"
        }
        if (schedule == ""){
            schedule = "В этот день нет пар!"
        }
        return schedule
    }

    private fun nearLesson(userGroup: String): String {
        var nearWeek = weekNum
        val parsedSchedule = parseSchedule(userGroup)
        var currDay = getDay(false)
        var currTime = getTime()
        if (currDay == "SUN"){
            currTime = "8:00"
            currDay = "MON"
            nearWeek = if (weekNum == 1){
                2
            }else{
                1
            }
        }
        val sortedByAllList : MutableList<ScheduleObject> = mutableListOf()
        val thisDaySchedule = parsedSchedule.filter { it.lesson.auditoriumReservation.reservationTime.week.toInt() == nearWeek }
        val sortedList = thisDaySchedule.sortedWith(compareBy{ it.lesson.auditoriumReservation.reservationTime.startTime })
        for (day in listOf("MON","TUE","WED","THU","FRI","SAT")){
            for (seObject in sortedList.filter { it.lesson.auditoriumReservation.reservationTime.weekDay == day }){
                sortedByAllList.add(seObject)
            }
        }
        var schedule = ""
        val codeTime = stringToCodeTime(currTime)
        val dayCode = stringToCodeDay(currDay)
        var breakFlag = false
        for (seObject in sortedByAllList){
            for (day in dayCode..6){
                for (hour in codeTime..107L step 1L){
                    if (codeToStringDay(day) == seObject.lesson.auditoriumReservation.reservationTime.weekDay &&
                        seObject.lesson.auditoriumReservation.reservationTime.startTime == hour){
                        schedule = "День недели: ${seObject.lesson.auditoriumReservation.reservationTime.weekDay} \n" +
                                "Название предмета: ${seObject.lesson.subject.title} (${seObject.lesson.subject.subjectType}).\n" +
                                "В ${codeToStringTime(seObject.lesson.auditoriumReservation.reservationTime.startTime)} \n" +
                                "Аудитория: ${seObject.lesson.auditoriumReservation.auditorium?.number?: "-"}\n" +
                                "Преподаватель: ${seObject.lesson.teacher?.initials?: "-"}\n"
                        breakFlag = true
                        break
                    }
                    if (breakFlag){break}
                }
                if (breakFlag){break}
            }
        }
        if (schedule == ""){
            val seObject = parsedSchedule.filter { it.lesson.auditoriumReservation.reservationTime.week.toInt() != nearWeek }.sortedWith(compareBy{ it.lesson.auditoriumReservation.reservationTime.startTime })[0]
            schedule = "День недели: ${seObject.lesson.auditoriumReservation.reservationTime.weekDay} \n" +
                    "Название предмета: ${seObject.lesson.subject.title} (${seObject.lesson.subject.subjectType}).\n" +
                    "В ${codeToStringTime(seObject.lesson.auditoriumReservation.reservationTime.startTime)} \n" +
                    "Аудитория: ${seObject.lesson.auditoriumReservation.auditorium?.number?: "-"}\n" +
                    "Преподаватель: ${seObject.lesson.teacher?.initials?: "-"}\n"
        }
        return schedule
    }

    private fun sendNotification(chatId: Long, responseText: String, update: Update?) {
        var responseMessage = SendMessage(chatId.toString(), responseText)
        responseMessage.enableMarkdown(true)
        var textChanger = true
        try {
            if (responseText.toInt() in 1000..9999 || (responseText[0] == '0' && responseText.length == 4)) {
                if (mapOfUsersGroups[chatId.toString()] != responseText && !groupsFilled){ //if fullGroupNumbers is Empty
                    connectToGroupList()
                }
                if (responseText in fullGroupNumbers) {
                    mapOfUsersGroups[chatId.toString()] = responseText
                    val newResponseText = "Ваш номер группы: $responseText"

                    //for DB
                    if (update!= null){
                        val userIdBuf = update.message.from.id.toString()
                        val groupNumberBuf = responseText
                        val name: String
                        val connectionDateTime = LocalDateTime.now().toString()

                        if (update.message.from.lastName != null && update.message.from.firstName != null)
                            name = update.message.from.lastName + update.message.from.firstName
                        else {
                            if (update.message.from.userName != null)
                                name = update.message.from.userName
                            else name = "UNKNOWN"
                        }

                        val DBBot_obj: DBBot = DBBot(userIdBuf, groupNumberBuf, name, connectionDateTime)
                        DBBot_obj.ConnectToDB()
                    }

                    textChanger = false
                    responseMessage = SendMessage(chatId.toString(), newResponseText)
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
        if (textChanger && responseText.length == 4){
            val groupNumbersWSeparator = fullGroupNumbers
            groupNumbersWSeparator.forEach { StringJoiner(", ").add(it) }
            val newResponseText: String = if (groupChecker in fullGroupNumbers){
                "Если хотите узнать расписание другой группы, введите номер группы корректно.\n" +
                        "Список групп, доступных для ввода: \n${groupNumbersWSeparator}"
            }else{
                "Введите номер группы корректно. Пример: $groupChecker"
            }
            responseMessage = SendMessage(chatId.toString(), newResponseText)
        }
        execute(responseMessage)
    }

    private fun connectToGroupList() {
        val connectToGroup =
            URL("https://digital.etu.ru/api/general/dicts/groups?scheduleId=publicated&withFaculty=true&withSemesterSeasons=true&withFlows=true").openConnection() as HttpURLConnection
        val groupsList: List<GroupID> = Gson().fromJson(connectToGroup.inputStream.bufferedReader().readText(), Array<GroupID>::class.java).toList()
        for (group in groupsList){
            fullGroupNumbers.add(group.fullNumber)
        }
        for (group in groupsList){
            groupId.add(group.id)
        }
        groupsFilled = true
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
    private fun codeToStringDay(code: Int): String {
        val day = when (code) {
            1 -> "MON"
            2 -> "TUE"
            3 -> "WED"
            4 -> "THU"
            5 -> "FRI"
            6 -> "SAT"
            else -> ""
        }
        return day
    }

    private fun stringToCodeDay(code: String): Int {
        val day = when (code) {
            "MON" -> 1
            "TUE" -> 2
            "WED" -> 3
            "THU" -> 4
            "FRI" -> 5
            "SAT" -> 6
            else -> 1
        }
        return day
    }

    private fun codeToStringTime(code: Long): String {
        val time = when (code) {
            100L -> "8:00"
            101L -> "9:50"
            102L -> "11:40"
            103L -> "13:40"
            104L -> "15:30"
            105L -> "17:20"
            106L -> "19:05"
            107L -> "20:50"
            else -> ""
        }
        return time
    }
    private fun stringToCodeTime(code: String): Long {
        var time = 100L
        if (code in listOf("9", "10")){
            time = 101L
        }
        if (code in listOf("11", "12")){
            time = 102L
        }
        if (code in listOf("13", "14")){
            time = 103L
        }
        if (code in listOf("15", "16")){
            time = 104L
        }
        if (code in listOf("17", "18")){
            time = 105L
        }
        if (code in listOf("19")){
            time = 106L
        }
        if (code in listOf("20", "21")){
            time = 107L
        }
        if (code in listOf("22", "23", "0", "24", "1", "2", "3", "4", "5", "6", "7", "8")){
            time = 100L
        }
        return time
    }
}