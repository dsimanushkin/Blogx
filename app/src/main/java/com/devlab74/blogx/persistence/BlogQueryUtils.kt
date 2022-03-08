package com.devlab74.blogx.persistence

import com.devlab74.blogx.models.BlogPost
import com.devlab74.blogx.persistence.BlogQueryUtils.Companion.ORDER_BY_ASC_DATE_UPDATED
import com.devlab74.blogx.persistence.BlogQueryUtils.Companion.ORDER_BY_ASC_USERNAME
import com.devlab74.blogx.persistence.BlogQueryUtils.Companion.ORDER_BY_DESC_DATE_UPDATED
import com.devlab74.blogx.persistence.BlogQueryUtils.Companion.ORDER_BY_DESC_USERNAME

/**
 * Utility class containing values that are important for DB itself, its operations, search orders
 */

class BlogQueryUtils {
    // Static values needed for proper DB Search and filtering
    companion object{
        // values
        const val BLOG_ORDER_ASC: String = ""
        const val BLOG_ORDER_DESC: String = "-"
        const val BLOG_FILTER_USERNAME = "username"
        const val BLOG_FILTER_DATE_UPDATED = "date_updated"

        const val ORDER_BY_ASC_DATE_UPDATED = BLOG_ORDER_ASC + BLOG_FILTER_DATE_UPDATED
        const val ORDER_BY_DESC_DATE_UPDATED = BLOG_ORDER_DESC + BLOG_FILTER_DATE_UPDATED
        const val ORDER_BY_ASC_USERNAME = BLOG_ORDER_ASC + BLOG_FILTER_USERNAME
        const val ORDER_BY_DESC_USERNAME = BLOG_ORDER_DESC + BLOG_FILTER_USERNAME
    }
}

// This function is returning correct page and query depending on user filter options
suspend fun BlogPostDao.returnOrderedBlogQuery(
    query: String,
    filterAndOrder: String,
    page: Int
): List<BlogPost> {
    when{
        filterAndOrder.contains(ORDER_BY_DESC_DATE_UPDATED) ->{
            return searchBlogPostsOrderByDateDESC(
                query = query,
                page = page)
        }

        filterAndOrder.contains(ORDER_BY_ASC_DATE_UPDATED) ->{
            return searchBlogPostsOrderByDateASC(
                query = query,
                page = page)
        }

        filterAndOrder.contains(ORDER_BY_DESC_USERNAME) ->{
            return searchBlogPostsOrderByAuthorDESC(
                query = query,
                page = page)
        }

        filterAndOrder.contains(ORDER_BY_ASC_USERNAME) ->{
            return searchBlogPostsOrderByAuthorASC(
                query = query,
                page = page)
        }
        else ->
            return searchBlogPostsOrderByDateASC(
                query = query,
                page = page
            )
    }
}