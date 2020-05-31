package com.bairwa.newsapp.repository

import com.bairwa.newsapp.api.RetrofitInstance
import com.bairwa.newsapp.database.ArticleDatabase
import com.bairwa.newsapp.models.Article

class NewsRepository(
    val db:ArticleDatabase
) {
    suspend fun getBreakingNews(countryCode:String,pageNumber:Int)=
        RetrofitInstance.api.getBreakingNews(countryCode,pageNumber)


    suspend fun searchingNews(searchQuery:String,pageNumber: Int)=
        RetrofitInstance.api.searchForNews(searchQuery,pageNumber)

    //adding all funtion which are present in Dao Interface
suspend fun upsert(article:Article)=db.getArticleDao().upsert(article)

    fun getSavedNews()=db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article: Article)=db.getArticleDao().deleteArticle(article)

}