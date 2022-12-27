package lLSBot.bot

import java.sql.Connection
import java.sql.DriverManager

class DBBot (userId: String, groupNumber: String, nameOrUsername: String, lastConnectionDateTime: String) {
    private var userId = userId
    private var groupNumber = groupNumber
    private var nameOrUsername = nameOrUsername
    private var lastConnectionDateTime = lastConnectionDateTime
    init {println ("Инициализация класса DBBot")}
    fun ConnectToDB(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/DB_Bot", "AdminForBot", "1234qwerasdf")
            println("Подключение успешно выполнено")
            //create table once
           /* val sql = """
                CREATE TABLE users(
                    userId NVARCHAR(50),
                    groupNumber NVARCHAR(50),
                    nameOrUsername NVARCHAR(255),
                    lastConnectionDateTime NVARCHAR(50)
                    );
                """.trimIndent()
            val query = connection.prepareStatement(sql)
            query.execute()*/

            var verificationOfExistence = false
            val searchUsers = "SELECT userId, groupNumber FROM users;"
            val searchUsersQuery = connection.prepareStatement(searchUsers)
            val result = searchUsersQuery.executeQuery()
            while (result.next()) {
                if (result.getString(1) == userId &&
                        result.getString(2) == groupNumber){
                    verificationOfExistence = true
                    break;
                }
            }

            if (verificationOfExistence == false)
                addingUser(connection)
            else
                updatingUser(connection)

        } catch (e: Exception) {
            e.printStackTrace()
            println("не удалось подключиться к базе данных")
        }
    }

    private fun addingUser(connection: Connection){
        try {
            val insertUserSql = "INSERT INTO users VALUES(?,?,?,?);"
            val queryInsertUser = connection.prepareStatement(insertUserSql)
            queryInsertUser.setString(1, userId)
            queryInsertUser.setString(2, groupNumber)
            queryInsertUser.setString(3, nameOrUsername)
            queryInsertUser.setString(4, lastConnectionDateTime)
            queryInsertUser.execute()
        }
        catch (e: Exception) {
            println("Ошибка заполнения данными")
        }
    }

    private fun updatingUser(connection: Connection){
        try{
            val updateUserSql = "UPDATE users SET lastConnectionDateTime = ? WHERE userId = ? AND groupNumber = ? LIMIT 10000;"
            val queryUpdateUser = connection.prepareStatement(updateUserSql)
            queryUpdateUser.setString(1,lastConnectionDateTime)
            queryUpdateUser.setString(2,userId)
            queryUpdateUser.setString(3,groupNumber)
            queryUpdateUser.execute()
        }
        catch (e: Exception){
            println("Ошибка обновления данных")
        }
    }

}