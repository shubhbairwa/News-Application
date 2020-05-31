package com.bairwa.newsapp.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bairwa.newsapp.R
import com.bairwa.newsapp.adapter.NewsAdapter
import com.bairwa.newsapp.ui.NewsActivity
import com.bairwa.newsapp.ui.NewsViewModel
import com.bairwa.newsapp.util.Constant
import com.bairwa.newsapp.util.Constant.Companion.SEARCH_NEWS_TIME_DELAY
import com.bairwa.newsapp.util.Resource
import kotlinx.android.synthetic.main.fragment_breaking_news.*
import kotlinx.android.synthetic.main.fragment_search_news.*
import kotlinx.android.synthetic.main.fragment_search_news.paginationProgressBar
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsFragment:Fragment(R.layout.fragment_search_news) {
    lateinit var viewModel: NewsViewModel

    lateinit var newsAdapter: NewsAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel=(activity as NewsActivity).viewModel //so we can get the viewModel of news activity in fragment

        setupRecyclerView()

        newsAdapter.setOnItemClickListener {
            var bundle=Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment
                ,bundle
            )
        }
var job:Job?=null
        etSearch.addTextChangedListener {
            job?.cancel()
            job= MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                it?.let {

                    if (it.toString().isNotEmpty()){
                        viewModel.searchNews(it.toString())

                                
                    }

                }
            }

        }



        viewModel.searchingNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let {
                        newsAdapter.differ.submitList(it.articles.toList())
                        val totalPages = it.totalResults / Constant.QUERY_PAGE_SIZE + 2
                        islastPage = viewModel.BreakingpageNumber == totalPages
                        if(islastPage){
                            rvSearchNews.setPadding(0, 0, 0, 0)
                        }
                    }

                }
                is Resource.Loading -> {
                    showProgressBar()
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {message->
                        Toast.makeText(activity,"ERROR", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        })
    }

    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.INVISIBLE

        isLoading=false
    }

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading=true
    }

    var isLoading = false
    var islastPage = false
    var isScrollingpage = false
    val scrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleitempositon = layoutManager.findFirstVisibleItemPosition() //first item
            val visibleItemCount = layoutManager.childCount //total number of visible item
            val totalItemCount = layoutManager.itemCount   //total number of item



            val isNotLoadingAndNotLastPage = !isLoading &&!islastPage
            val isAtLastItem = firstVisibleitempositon + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleitempositon >= 0
            val isTotaolMoreThanVisible = totalItemCount >= Constant.QUERY_PAGE_SIZE
            val shouldPaginate =
                isNotLoadingAndNotLastPage && isNotAtBeginning && isAtLastItem && isTotaolMoreThanVisible
                        && isScrollingpage
            if (shouldPaginate) {
                viewModel.searchNews(etSearch.text.toString())
                isScrollingpage = false
            }else{
                rvSearchNews.setPadding(0,0,0,0)
            }


        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) { //it means we are scrolling
                isScrollingpage = true

            }
        }
    }


    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }
    }
}