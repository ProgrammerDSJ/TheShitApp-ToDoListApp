package com.example.theshitapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.theshitapp.adapter.ViewPagerAdapter
import com.example.theshitapp.databinding.FragmentScheduleContainerBinding
import com.google.android.material.tabs.TabLayoutMediator

class ScheduleContainerFragment : Fragment() {
    
    private var _binding: FragmentScheduleContainerBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleContainerBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }
    
    private fun setupViewPager() {
        val fragments = listOf<Fragment>(
            TasksFragment.newInstance(),
            DailyTasksFragment.newInstance(),
            WeeklyScheduleFragment.newInstance(),
            CompletedTasksFragment.newInstance()
        )
        
        val viewPagerAdapter = ViewPagerAdapter(requireActivity(), fragments)
        binding.scheduleViewPager.adapter = viewPagerAdapter
        
        TabLayoutMediator(binding.scheduleTabLayout, binding.scheduleViewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Tasks"
                }
                1 -> {
                    tab.text = "Daily Tasks"
                }
                2 -> {
                    tab.text = "Weekly Schedule"
                }
                3 -> {
                    tab.text = "Completed Tasks"
                }
            }
        }.attach()
    }
    
    fun refreshCurrentTab() {
        val currentItem = binding.scheduleViewPager.currentItem
        val currentFragment = childFragmentManager.findFragmentByTag("f$currentItem")
        
        when (currentFragment) {
            is TasksFragment -> currentFragment.refresh()
            is DailyTasksFragment -> currentFragment.refreshTasks()
            is WeeklyScheduleFragment -> {} // The weekly schedule will refresh itself when it becomes visible
            is CompletedTasksFragment -> currentFragment.refreshCompletedTasks()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = ScheduleContainerFragment()
    }
} 