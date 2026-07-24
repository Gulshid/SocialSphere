package com.gulshid.socialsphere.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gulshid.socialsphere.data.model.Notification
import com.gulshid.socialsphere.databinding.FragmentNotificationsBinding
import com.gulshid.socialsphere.ui.profile.UserProfileActivity
import com.gulshid.socialsphere.utils.Resource
import com.gulshid.socialsphere.utils.gone
import com.gulshid.socialsphere.utils.toast
import com.gulshid.socialsphere.utils.visible

/**
 * Shows likes, comments and new followers for the current user. Tapping any
 * notification opens the profile of whoever triggered it.
 */
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationsViewModel by viewModels()
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NotificationAdapter { notification -> onNotificationClicked(notification) }
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter

        viewModel.notificationsState.observe(viewLifecycleOwner) { state -> render(state) }
        viewModel.loadNotifications()
    }

    override fun onResume() {
        super.onResume()
        // Refresh whenever the tab becomes visible again so new activity shows up promptly.
        viewModel.loadNotifications()
    }

    private fun render(state: Resource<List<Notification>>) {
        when (state) {
            is Resource.Loading -> {
                binding.progressBar.visible()
                binding.emptyState.gone()
            }
            is Resource.Success -> {
                binding.progressBar.gone()
                adapter.submitList(state.data)
                val isEmpty = state.data.isEmpty()
                binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
                binding.rvNotifications.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
            is Resource.Error -> {
                binding.progressBar.gone()
                requireContext().toast(state.message)
            }
        }
    }

    private fun onNotificationClicked(notification: Notification) {
        val intent = Intent(requireContext(), UserProfileActivity::class.java)
        intent.putExtra(UserProfileActivity.EXTRA_UID, notification.actorId)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
