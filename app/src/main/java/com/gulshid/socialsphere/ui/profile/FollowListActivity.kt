package com.gulshid.socialsphere.ui.profile


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gulshid.socialsphere.R
import com.gulshid.socialsphere.databinding.ActivityFollowListBinding
import com.gulshid.socialsphere.ui.search.FollowableUser
import com.gulshid.socialsphere.ui.search.UserAdapter
import com.gulshid.socialsphere.utils.Resource
import com.gulshid.socialsphere.utils.gone
import com.gulshid.socialsphere.utils.visible
import com.gulshid.socialsphere.utils.toast

/**
 * Shows either the followers or following list for [EXTRA_UID], reached by
 * tapping the follower/following counts on a profile screen. Reuses the same
 * UserAdapter/FollowableUser plumbing as search, so Follow/Following works
 * live from this list too.
 */
class FollowListActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_UID = "extra_uid"
        private const val EXTRA_TYPE = "extra_type"

        fun start(context: Context, uid: String, type: FollowListType) {
            val intent = Intent(context, FollowListActivity::class.java)
            intent.putExtra(EXTRA_UID, uid)
            intent.putExtra(EXTRA_TYPE, type.name)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityFollowListBinding
    private val viewModel: FollowListViewModel by viewModels()
    private lateinit var adapter: UserAdapter
    private var type: FollowListType = FollowListType.FOLLOWERS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = intent.getStringExtra(EXTRA_UID)
        type = intent.getStringExtra(EXTRA_TYPE)?.let {
            runCatching { FollowListType.valueOf(it) }.getOrNull()
        } ?: FollowListType.FOLLOWERS

        if (uid.isNullOrBlank()) {
            finish()
            return
        }

        binding.toolbar.title = getString(
            if (type == FollowListType.FOLLOWERS) R.string.followers else R.string.following
        )
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.tvEmpty.text = getString(
            if (type == FollowListType.FOLLOWERS) R.string.no_followers_yet else R.string.no_following_yet
        )

        adapter = UserAdapter(
            onUserClicked = { user ->
                val intent = Intent(this, UserProfileActivity::class.java)
                intent.putExtra(UserProfileActivity.EXTRA_UID, user.uid)
                startActivity(intent)
            },
            onFollowClicked = { item -> viewModel.toggleFollow(item) }
        )
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter

        viewModel.usersState.observe(this) { state -> render(state) }
        viewModel.load(uid, type)
    }

    private fun render(state: Resource<List<FollowableUser>>) {
        when (state) {
            is Resource.Loading -> binding.progressBar.visible()
            is Resource.Success -> {
                binding.progressBar.gone()
                adapter.submitList(state.data)
                binding.tvEmpty.visibility =
                    if (state.data.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                binding.rvUsers.visibility =
                    if (state.data.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
            }
            is Resource.Error -> {
                binding.progressBar.gone()
                toast(state.message)
            }
        }
    }
}