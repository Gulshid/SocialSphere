package com.gulshid.socialsphere.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gulshid.socialsphere.databinding.FragmentSearchBinding
import com.gulshid.socialsphere.ui.profile.UserProfileActivity
import com.gulshid.socialsphere.utils.Resource
import com.gulshid.socialsphere.utils.toast

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UserAdapter(
            onUserClicked = { user ->
                val intent = Intent(requireContext(), UserProfileActivity::class.java)
                intent.putExtra(UserProfileActivity.EXTRA_UID, user.uid)
                startActivity(intent)
            },
            onFollowClicked = { item -> viewModel.toggleFollow(item) }
        )
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        viewModel.searchResults.observe(viewLifecycleOwner) { state -> render(state) }
    }

    private fun render(state: Resource<List<FollowableUser>>) {
        when (state) {
            is Resource.Loading -> Unit
            is Resource.Success -> {
                adapter.submitList(state.data)
                binding.tvEmpty.visibility = if (state.data.isEmpty()) View.VISIBLE else View.GONE
                binding.rvUsers.visibility = if (state.data.isEmpty()) View.GONE else View.VISIBLE
            }
            is Resource.Error -> requireContext().toast(state.message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
