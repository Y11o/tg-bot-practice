package lLSBot.groupsID

// To parse the JSON, install Klaxon and do:
//
//   val welcome9 = Welcome9.fromJson(jsonString)
import com.beust.klaxon.*

private fun <T> Klaxon.convert(k: kotlin.reflect.KClass<*>, fromJson: (JsonValue) -> T, toJson: (T) -> String, isUnion: Boolean = false) =
    this.converter(object: Converter {
        @Suppress("UNCHECKED_CAST")
        override fun toJson(value: Any)        = toJson(value as T)
        override fun fromJson(jv: JsonValue)   = fromJson(jv) as Any
        override fun canConvert(cls: Class<*>) = cls == k.java || (isUnion && cls.superclass == k.java)
    })

private val klaxon = Klaxon()
    .convert(Title::class,          { Title.fromValue(it.string!!) },          { "\"${it.value}\"" })
    .convert(Type::class,           { Type.fromValue(it.string!!) },           { "\"${it.value}\"" })
    .convert(EducationLevel::class, { EducationLevel.fromValue(it.string!!) }, { "\"${it.value}\"" })
    .convert(Season::class,         { Season.fromValue(it.string!!) },         { "\"${it.value}\"" })
    .convert(StudyYears::class,     { StudyYears.fromValue(it.string!!) },     { "\"${it.value}\"" })
    .convert(StudyingType::class,   { StudyingType.fromValue(it.string!!) },   { "\"${it.value}\"" })

class Welcome9(elements: Collection<GroupID>) : ArrayList<GroupID>(elements) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = Welcome9(klaxon.parseArray<GroupID>(json)!!)
    }
}

data class GroupID (
    val fullNumber: String,
    val id: Long,
    val number: String,
    val studentCount: Long,
    val course: Long,
    val semester: Long,
    val index: Any? = null,
    val studyingType: StudyingType,
    val educationLevel: EducationLevel,

    @Json(name = "alienId")
    val alienID: Long,

    val studyYears: StudyYears,
    val semesterSeasons: List<SemesterSeason>,
    val department: Department,
    val groupSets: List<GroupSet>
)

data class Department (
    val id: Long,
    val title: String,
    val longTitle: String,
    val type: Type,
    val faculty: Faculty
)

data class Faculty (
    val id: Long,
    val title: Title
)

enum class Title(val value: String) {
    Гф("ГФ"),
    Инпротех("ИНПРОТЕХ"),
    Фибс("ФИБС"),
    Фкти("ФКТИ"),
    Фрт("ФРТ"),
    Фэа("ФЭА"),
    Фэл("ФЭЛ");

    companion object {
        public fun fromValue(value: String): Title = when (value) {
            "ГФ"       -> Гф
            "ИНПРОТЕХ" -> Инпротех
            "ФИБС"     -> Фибс
            "ФКТИ"     -> Фкти
            "ФРТ"      -> Фрт
            "ФЭА"      -> Фэа
            "ФЭЛ"      -> Фэл
            else       -> throw IllegalArgumentException()
        }
    }
}

enum class Type(val value: String) {
    Foreign("foreign"),
    Normal("normal");

    companion object {
        public fun fromValue(value: String): Type = when (value) {
            "foreign" -> Foreign
            "normal"  -> Normal
            else      -> throw IllegalArgumentException()
        }
    }
}

enum class EducationLevel(val value: String) {
    Бак("бак"),
    Маг("маг"),
    Спец("спец");

    companion object {
        public fun fromValue(value: String): EducationLevel = when (value) {
            "бак"  -> Бак
            "маг"  -> Маг
            "спец" -> Спец
            else   -> throw IllegalArgumentException()
        }
    }
}

data class GroupSet (
    val id: Long,
    val title: String,

    @Json(name = "GroupInSet")
    val groupInSet: GroupInSet
)

data class GroupInSet (
    val index: Long
)

data class SemesterSeason (
    val id: Long,
    val season: Season,
    val year: Long,
    val startDate: String,
    val endDate: String,

    @Json(name = "GroupSemesterSeason")
    val groupSemesterSeason: GroupSemesterSeason
)

data class GroupSemesterSeason (
    val semesterStart: String? = null,
    val semesterEnd: String? = null,
    val examStart: String? = null,
    val examEnd: String? = null
)

enum class Season(val value: String) {
    Autumn("autumn");

    companion object {
        public fun fromValue(value: String): Season = when (value) {
            "autumn" -> Autumn
            else     -> throw IllegalArgumentException()
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

enum class StudyingType(val value: String) {
    Веч("веч"),
    Заоч("заоч"),
    Оч("оч");

    companion object {
        public fun fromValue(value: String): StudyingType = when (value) {
            "веч"  -> Веч
            "заоч" -> Заоч
            "оч"   -> Оч
            else   -> throw IllegalArgumentException()
        }
    }
}


