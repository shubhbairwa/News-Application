package com.bairwa.newsapp.ui.fragment

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bairwa.newsapp.R
import com.bairwa.newsapp.adapter.NewsAdapter
import com.bairwa.newsapp.ui.NewsActivity
import com.bairwa.newsapp.ui.NewsViewModel
import com.bairwa.newsapp.util.Constant.Companion.QUERY_PAGE_SIZE
import com.bairwa.newsapp.util.Resource
import kotlinx.android.synthetic.main.fragment_breaking_news.*

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel //so we can get the viewModel of news activity in fragment
        setupRecyclerView()


        newsAdapter.setOnItemClickListener {
            Log.e("Nee=w=", "getting click")
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment, bundle
            )

        }

        viewModel.breakingNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let {
                        newsAdapter.differ.submitList(it.articles.toList())//converting to list because of diff util
                        val totalPages = it.totalResults / QUERY_PAGE_SIZE + 2
                        islastPage = viewModel.BreakingpageNumber == totalPages

                    }

                }
                is Resource.Loading -> {
                    showProgressBar()
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        Log.e("error",it)
                        Toast.makeText(activity,"Error: ${it}",Toast.LENGTH_SHORT).show()
                    }

                }
            }
        })
    }

    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
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
            val isTotaolMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate =
                isNotLoadingAndNotLastPage && isNotAtBeginning && isAtLastItem && isTotaolMoreThanVisible
                        && isScrollingpage
            if (shouldPaginate) {
                viewModel.getBreakingNews("in")
                isScrollingpage = false
            }else{
                rvBreakingNews.setPadding(0,0,0,0)
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
        rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }
    }
}