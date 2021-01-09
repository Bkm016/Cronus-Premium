package ink.ptms.cronus.database

import ink.ptms.cronus.database.data.DataPlayer
import ink.ptms.cronus.database.impl.DatabaseMongoDB
import ink.ptms.cronus.database.impl.DatabaseSQL
import org.bson.Document

object DataMigration {

    fun toMongoDB() {
        var from: DatabaseSQL? = null
        var to: DatabaseMongoDB? = null
        try {
            from = DatabaseSQL()
            to = DatabaseMongoDB()
        } catch (t: Throwable) {
        }
        if (from?.isConnected == false) {
            println("[Cronus Migration] SQL 数据库尚未连接.")
            return
        }
        if (to?.connected == false) {
            println("[Cronus Migration] MongoDB 数据库尚未连接.")
            return
        }
        println("[Cronus Migration] 正在读取原数据.")
        var i = 0
        val dataMap = LinkedHashMap<String, DataPlayer>()
        try {
            from!!.table.select().to(from.dataSource).resultNext {
                if (i++ % 100 == 0) {
                    println("[Cronus Migration]  ... $i")
                }
                dataMap.put(it.getString("player"), DataPlayer().run {
                    this.readBase64(it.getString("data"))
                    this
                })
            }.run()
        } catch (t: Throwable) {
        }
        println("[Cronus Migration] 正在写入新数据.")
        try {
            i = 0
            dataMap.forEach { (k, v) ->
                if (i++ % 100 == 0) {
                    println("[Cronus Migration]  ... $i")
                }
                to!!.mongoCollectionData!!.insertOne(Document.parse(to.serializer.toJson(v)).append("player", k))
            }
        } catch (t: Throwable) {
        }
        println("[Cronus Migration] 完成.")
    }
}