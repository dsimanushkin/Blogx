package com.devlab74.blogx.ui.main.blog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.devlab74.blogx.databinding.LayoutBlogListItemBinding
import com.devlab74.blogx.databinding.LayoutNoMoreResultsBinding
import com.devlab74.blogx.models.BlogPost
import com.devlab74.blogx.util.DateUtils
import com.devlab74.blogx.util.GenericViewHolder

/**
 * BlogListAdapter Class
 *
 * RecyclerView adapter
 */

class BlogListAdapter(
    private val interaction: Interaction? = null,
    private val requestManager: RequestManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val NO_MORE_RESULTS = -1
    private val BLOG_ITEM = 0
    private val NO_MORE_RESULTS_BLOG_MARKER = BlogPost(
        "null",
        "",
        "",
        "",
        0,
        ""
    )

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BlogPost>() {
        override fun areItemsTheSame(oldItem: BlogPost, newItem: BlogPost): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BlogPost, newItem: BlogPost): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(
        BlogRecyclerChangeCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
    )

    internal inner class BlogRecyclerChangeCallback(
        private val adapter: BlogListAdapter
    ): ListUpdateCallback {
        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapter.notifyItemRangeChanged(position, count, payload)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyDataSetChanged()
        }

        override fun onInserted(position: Int, count: Int) {
            adapter.notifyItemRangeChanged(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            adapter.notifyDataSetChanged()
        }

    }

    fun preloadGlideImages(
        requestManager: RequestManager,
        list: List<BlogPost>
    ) {
        for (blogPost in list) {
            requestManager
                .load(blogPost.image)
                .preload()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when(viewType) {
            NO_MORE_RESULTS -> {
                return GenericViewHolder(
                    LayoutNoMoreResultsBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            BLOG_ITEM -> {
                return BlogViewHolder(
                    LayoutBlogListItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
            else -> {
                return BlogViewHolder(
                    LayoutBlogListItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BlogViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (differ.currentList[position].id != "null") {
            return BLOG_ITEM
        }
        return NO_MORE_RESULTS
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(blogList: List<BlogPost>, isQueryExhausted: Boolean) {
        val newList = blogList.toMutableList()
        if (isQueryExhausted) {
            newList.add(NO_MORE_RESULTS_BLOG_MARKER)
        }

        val commitCallback = Runnable {
            interaction?.restoreListPosition()
        }

        differ.submitList(newList, commitCallback)
    }

    class BlogViewHolder
    constructor(
        private val binding: LayoutBlogListItemBinding,
        private val requestManager: RequestManager,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BlogPost) = with(binding) {
            binding.root.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            requestManager
                .load(item.image)
                .transition(withCrossFade())
                .into(binding.blogImage)
            binding.blogTitle.text = item.title
            binding.blogAuthor.text = item.username
            binding.blogUpdateDate.text = DateUtils.convertLongToStringDate(item.dateUpdated)
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: BlogPost)

        fun restoreListPosition()
    }
}