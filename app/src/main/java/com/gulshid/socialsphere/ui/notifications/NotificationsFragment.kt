package com.gulshid.socialsphere.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gulshid.socialsphere.databinding.FragmentNotificationsBinding

/**
 * Displays likes, comments, and new-follower notifications.
 * Wire this to a Firestore "notifications/{uid}/items" collection
 * (typically populated by a Cloud Function trigger) for a production build.
 */
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
