package com.bairwa.newsapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bairwa.newsapp.models.Article

@Database(
    entities = [Article::class] //array of tables
,version = 1

)
@TypeConverters(Convertor::class)
abstract class ArticleDatabase: RoomDatabase() {

abstract fun getArticleDao():ArticleDao

    companion object{ // wecreate companion object so that we create the database

//singleton object
        @Volatile //so that other thread can see if it change its instance
        private var instance:ArticleDatabase?=null //checking null
        private val LOCK=Any()


        operator fun invoke(context: Context)= instance?: synchronized(LOCK){  // it make instance for one thread at a time so no ther thread get instancw
            instance?:createDatabase(context).also{ //check again for null
                instance=it  //here set the instance of create database
            }
        }

        private fun createDatabase(context: Context)=
            Room.databaseBuilder(
                context.applicationContext,ArticleDatabase::class.java
            ,"article_db.db"
            ).build()

    }
}