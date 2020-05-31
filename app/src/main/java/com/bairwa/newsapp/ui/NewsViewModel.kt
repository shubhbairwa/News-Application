package com.bairwa.newsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.net.TransportInfo
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bairwa.newsapp.NewsApplication
import com.bairwa.newsapp.models.Article
import com.bairwa.newsapp.models.NewsResponse
import com.bairwa.newsapp.repository.NewsRepository
import com.bairwa.newsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    app: Application,
    val newsRepository: NewsRepository
) : AndroidViewModel(app) {
    /*we cant call constructor here so that why we create viewModelProviderFatcory
     */

    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var BreakingpageNumber = 1
    val searchingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchingPageNumber = 1
    var breakinngNewsResponse: NewsResponse? = null
    var SearchNewsResponse: NewsResponse? = null


    init {
        getBreakingNews("in")

    }

    fun getBreakingNews(countryCode: String) =
        viewModelScope.launch { //so that data would not be lost in changing configuration
  safeBreakingNewsCall(countryCode)

        }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
       safeSearchNewsCall(searchQuery)
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                BreakingpageNumber++
                if (breakinngNewsResponse == null) {
                    breakinngNewsResponse = resultResponse
                } else {
                    val oldArticles = breakinngNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(breakinngNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(message = response.message())


    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchingPageNumber++
                if (SearchNewsResponse == null) {

                    SearchNewsResponse = resultResponse
                } else {
                    val oldArticles = SearchNewsResponse?.articles
                    val newArticles = resultResponse.articles

                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(SearchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(message = response.message())


    }

    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getSavedNews() = newsRepository.getSavedNews()
    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }


    private suspend fun safeBreakingNewsCall(countryCode: String){
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val response = newsRepository.getBreakingNews(countryCode, BreakingpageNumber)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            }
            else{
                breakingNews.postValue(Resource.Error("No Internet Connection"))
            }
        }catch (t:Throwable){

            when(t){
                is IOException-> breakingNews.postValue(Resource.Error("Network Failure"))
            else->breakingNews.postValue(Resource.Error("Conversion Failure"))
            }
        }
    }



    private suspend fun safeSearchNewsCall(searchQuery: String){
        searchingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val response = newsRepository.searchingNews(searchQuery,searchingPageNumber)
                searchingNews.postValue(handleSearchNewsResponse(response))
            }
            else{
                searchingNews.postValue(Resource.Error("No Internet Connection"))
            }
        }catch (t:Throwable){

            when(t){
                is IOException-> searchingNews.postValue(Resource.Error("Network Failure"))
                else->searchingNews.postValue(Resource.Error("Conversion Failure"))
            }
        }
    }


    private fun hasInternetConnection(): Boolean { // you can check anywhere by this method
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetworkState = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetworkState) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }


        }
        connectivityManager.activeNetworkInfo.run {
            return when (type) {
                TYPE_WIFI -> true
                TYPE_MOBILE -> true
                TYPE_ETHERNET -> true
                else -> false

            }


        }
        return false
    }

}