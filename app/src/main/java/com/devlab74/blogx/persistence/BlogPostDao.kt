package com.devlab74.blogx.persistence

import androidx.room.*
import com.devlab74.blogx.models.BlogPost
import com.devlab74.blogx.util.Constants.Companion.PAGINATION_PAGE_SIZE

/**
 * DAO interface is responsible for all DB operations related to BlogPost
 */

@Dao
interface BlogPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blogPost: BlogPost): Long

    @Query("""
        SELECT * FROM blog_post 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%' 
        ORDER BY date_updated DESC LIMIT (:page * :pageSize)
    """)
    suspend fun searchBlogPostsOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<BlogPost>

    @Query("""
        SELECT * FROM blog_post 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%' 
        ORDER BY date_updated ASC LIMIT (:page * :pageSize)
    """)
    suspend fun searchBlogPostsOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<BlogPost>

    @Query("""
        SELECT * FROM blog_post 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%' 
        ORDER BY username DESC LIMIT (:page * :pageSize)
    """)
    suspend fun searchBlogPostsOrderByAuthorDESC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<BlogPost>

    @Query("""
        SELECT * FROM blog_post 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%' 
        ORDER BY username ASC LIMIT (:page * :pageSize)
    """)
    suspend fun searchBlogPostsOrderByAuthorASC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<BlogPost>

    @Delete
    suspend fun deleteBlogPost(blogPost: BlogPost)

    @Query("""
        UPDATE blog_post 
        SET title = :title,
        body = :body,
        image = :image
        WHERE id = :id
    """)
    suspend fun updateBlogPost(id: String, title: String, body: String, image: String)
}