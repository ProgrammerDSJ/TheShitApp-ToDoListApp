package com.example.theshitapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.theshitapp.databinding.FragmentBoardsContainerBinding

class BoardsContainerFragment : Fragment() {
    
    private var _binding: FragmentBoardsContainerBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoardsContainerBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Add the BoardsFragment to this container
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(binding.boardsContainer.id, BoardsFragment.newInstance())
                .commit()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = BoardsContainerFragment()
    }
} 