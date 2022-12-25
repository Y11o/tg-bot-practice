package lLSBot.groupSchedule

import com.beust.klaxon.*
private fun <T> Klaxon.convert(k: kotlin.reflect.KClass<*>, fromJson: (JsonValue) -> T, toJson: (T) -> String, isUnion: Boolean = false) =
    this.converter(object: Converter {
        @Suppress("UNCHECKED_CAST")
        override fun toJson(value: Any)        = toJson(value as T)
        override fun fromJson(jv: JsonValue)   = fromJson(jv) as Any
        override fun canConvert(cls: Class<*>) = cls == k.java || (isUnion && cls.superclass == k.java)
    })

private val klaxon = Klaxon()
    .convert(Form::class,                      { Form.fromValue(it.string!!) },                      { "\"${it.value}\"" })
    .convert(AuditoriumReservationType::class, { AuditoriumReservationType.fromValue(it.string!!) }, { "\"${it.value}\"" })
    .convert(Degree::class,                    { Degree.fromValue(it.string!!) },                    { "\"${it.value}\"" })
    .convert(Phone::class,                     { Phone.fromValue(it.string!!) },                     { "\"${it.value}\"" })
    .convert(Rank::class,                      { Rank.fromValue(it.string!!) },                      { "\"${it.value}\"" })
    .convert(Role::class,                      { Role.fromValue(it.string!!) },                      { "\"${it.value}\"" })
    .convert(ControlType::class,               { ControlType.fromValue(it.string!!) },               { "\"${it.value}\"" })
    .convert(LongTitle::class,                 { LongTitle.fromValue(it.string!!) },                 { "\"${it.value}\"" })
    .convert(Title::class,                     { Title.fromValue(it.string!!) },                     { "\"${it.value}\"" })
    .convert(DepartmentType::class,            { DepartmentType.fromValue(it.string!!) },            { "\"${it.value}\"" })
    .convert(StudyYears::class,                { StudyYears.fromValue(it.string!!) },                { "\"${it.value}\"" })
    .convert(SubjectType::class,               { SubjectType.fromValue(it.string!!) },               { "\"${it.value}\"" })

class Welcome10(elements: Collection<GroupSchedule>) : ArrayList<GroupSchedule>(elements) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = Welcome10(klaxon.parseArray<GroupSchedule>(json)!!)
    }
}

data class GroupSchedule (
    val fullNumber: String,
    val id: Long,
    val number: String,
    val studentCount: Long,
    val course: Long,
    val semester: Long,
    val index: Any? = null,
    val studyingType: String,
    val educationLevel: String,

    @Json(name = "alienId")
    val alienID: Long,

    val studyYears: StudyYears,
    val semesterSeasons: List<SemesterSeason>,
    val groupSets: List<GroupSet>,
    val scheduleObjects: List<ScheduleObject>
)

data class GroupSet (
    val id: Long,
    val title: String,

    @Json(name = "GroupInSet")
    val groupInSet: GroupInSet
)

data class GroupInSet (
    val index: Long
)

data class ScheduleObject (
    @Json(name = "groupId")
    val groupID: Long,

    val id: Long,

    @Json(name = "lessonId")
    val lessonID: Long,

    val comment: String,
    val form: Form,
    val block: String,
    val url: Any? = null,
    val lesson: Lesson
)

enum class Form(val value: String) {
    Standard("standard");

    companion object {
        public fun fromValue(value: String): Form = when (value) {
            "standard" -> Standard
            else       -> throw IllegalArgumentException()
        }
    }
}

data class Lesson (
    val id: Long,
    val newTeacherDateStart: Any? = null,
    val newTeacherDateEnd: Any? = null,
    val newAudDateStart: Any? = null,
    val newAudDateEnd: Any? = null,
    val isVacant: Boolean,
    val groupNumber: String,
    val auditoriumReservation: AuditoriumReservation,
    val newAuditoriumReservation: Any? = null,
    val newTeacher: Any? = null,
    val subject: Subject,
    val teacher: Teacher? = null,
    val secondTeacher: Teacher? = null
)

data class AuditoriumReservation (
    val id: Long,
    val type: AuditoriumReservationType,
    val description: String,
    val updatedAt: String,
    val auditoriumNumber: String? = null,

    @Json(name = "examId")
    val examID: Any? = null,

    @Json(name = "scheduleId")
    val scheduleID: Long,

    @Json(name = "scheduleObjectId")
    val scheduleObjectID: Long,

    @Json(name = "examObjectId")
    val examObjectID: Any? = null,

    val reservationTime: ReservationTime,
    val auditorium: Auditorium? = null
)

data class Auditorium (
    val displayName: String,
    val number: String,
    val alias: Any? = null
)

data class ReservationTime (
    val id: Long,
    val startTime: Long,
    val endTime: Long,
    val startDate: String,
    val endDate: String,
    val repeat: Any? = null,
    val weekDay: String,
    val week: String
)

enum class AuditoriumReservationType(val value: String) {
    Schedule("schedule");

    companion object {
        public fun fromValue(value: String): AuditoriumReservationType = when (value) {
            "schedule" -> Schedule
            else       -> throw IllegalArgumentException()
        }
    }
}

data class Teacher (
    val initials: String,
    val isTeacher: Boolean,
    val id: Long,
    val surname: String,
    val name: String,
    val midname: String,
    val phone: Phone? = null,
    val birthday: String,
    val roles: List<Role>,
    val workDepartments: List<String>? = null,
    val position: String,
    val degree: Degree? = null,
    val rank: Rank? = null,
    val email: String? = null,

    @Json(name = "alienId")
    val alienID: Long,

    @Json(name = "groupId")
    val groupID: Any? = null,

    val personalNumber: String,
    val isEmailNotif: Boolean
)

enum class Degree(val value: String) {
    ДокторНаук("Доктор наук"),
    КандидатНаук("Кандидат наук");

    companion object {
        public fun fromValue(value: String): Degree = when (value) {
            "Доктор наук"   -> ДокторНаук
            "Кандидат наук" -> КандидатНаук
            else            -> throw IllegalArgumentException()
        }
    }
}

enum class Phone(val value: String) {
    Empty(""),
    The79219394016("+79219394016");

    companion object {
        public fun fromValue(value: String): Phone = when (value) {
            ""             -> Empty
            "+79219394016" -> The79219394016
            else           -> throw IllegalArgumentException()
        }
    }
}

enum class Rank(val value: String) {
    Доцент("Доцент"),
    Профессор("Профессор");

    companion object {
        public fun fromValue(value: String): Rank = when (value) {
            "Доцент"    -> Доцент
            "Профессор" -> Профессор
            else        -> throw IllegalArgumentException()
        }
    }
}

enum class Role(val value: String) {
    DepartmentDispatcher("departmentDispatcher"),
    Student("student"),
    Teacher("teacher"),
    Worker("worker");

    companion object {
        public fun fromValue(value: String): Role = when (value) {
            "departmentDispatcher" -> DepartmentDispatcher
            "student"              -> Student
            "teacher"              -> Teacher
            "worker"               -> Worker
            else                   -> throw IllegalArgumentException()
        }
    }
}

data class Subject (
    val id: Long,
    val title: String,
    val englishTitle: Any? = null,
    val shortTitle: String,
    val subjectType: SubjectType,
    val controlType: ControlType? = null,
    val semester: Long,

    @Json(name = "alienId")
    val alienID: Long,

    val studyYears: StudyYears,
    val department: Department
)

enum class ControlType(val value: String) {
    ЗчО("ЗчО"),
    Экз("Экз");

    companion object {
        public fun fromValue(value: String): ControlType = when (value) {
            "ЗчО" -> ЗчО
            "Экз" -> Экз
            else  -> throw IllegalArgumentException()
        }
    }
}

data class Department (
    val id: Long,
    val title: Title,
    val longTitle: LongTitle? = null,
    val type: DepartmentType
)

enum class LongTitle(val value: String) {
    КафедраАвтоматикиИПроцессовУправления("Кафедра автоматики и процессов управления"),
    КафедраИнформационноИзмерительныхСистемИТехнологий("Кафедра информационно-измерительных систем и технологий"),
    КафедраСистемАвтоматизированногоПроектирования("Кафедра систем автоматизированного проектирования");

    companion object {
        public fun fromValue(value: String): LongTitle = when (value) {
            "Кафедра автоматики и процессов управления"               -> КафедраАвтоматикиИПроцессовУправления
            "Кафедра информационно-измерительных систем и технологий" -> КафедраИнформационноИзмерительныхСистемИТехнологий
            "Кафедра систем автоматизированного проектирования"       -> КафедраСистемАвтоматизированногоПроектирования
            else                                                      -> throw IllegalArgumentException()
        }
    }
}

enum class Title(val value: String) {
    ВоенныйУчебныйЦентр("Военный учебный центр"),
    КафАПУ("каф.АПУ"),
    КафИИСТ("каф.ИИСТ"),
    КафСАПР("каф.САПР");

    companion object {
        public fun fromValue(value: String): Title = when (value) {
            "Военный учебный центр" -> ВоенныйУчебныйЦентр
            "каф.АПУ"               -> КафАПУ
            "каф.ИИСТ"              -> КафИИСТ
            "каф.САПР"              -> КафСАПР
            else                    -> throw IllegalArgumentException()
        }
    }
}

enum class DepartmentType(val value: String) {
    Military("military"),
    Normal("normal");

    companion object {
        public fun fromValue(value: String): DepartmentType = when (value) {
            "military" -> Military
            "normal"   -> Normal
            else       -> throw IllegalArgumentException()
        }
    }
}

enum class StudyYears(val value: String) {
    The20222023("2022-2023");

    companion object {
        public fun fromValue(value: String): StudyYears = when (value) {
            "2022-2023" -> The20222023
            else        -> throw IllegalArgumentException()
        }
    }
}

enum class SubjectType(val value: String) {
    Лаб("Лаб"),
    Лек("Лек"),
    Пр("Пр");

    companion object {
        public fun fromValue(value: String): SubjectType = when (value) {
            "Лаб" -> Лаб
            "Лек" -> Лек
            "Пр"  -> Пр
            else  -> throw IllegalArgumentException()
        }
    }
}

data class SemesterSeason (
    val id: Long,
    val season: String,
    val year: Long,
    val startDate: String,
    val endDate: String,

    @Json(name = "GroupSemesterSeason")
    val groupSemesterSeason: GroupSemesterSeason
)

data class GroupSemesterSeason (
    val semesterStart: String,
    val semesterEnd: String,
    val examStart: String,
    val examEnd: String,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: Any? = null,

    @Json(name = "groupId")
    val groupID: Long,

    @Json(name = "semesterSeasonId")
    val semesterSeasonID: Long
)
