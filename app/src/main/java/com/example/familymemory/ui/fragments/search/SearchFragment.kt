package com.example.familymemory.ui.fragments.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.familymemory.data.VideoItem
import com.example.familymemory.databinding.FragmentSearchBinding
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var storageRef: FirebaseStorage
    private lateinit var searchAdapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSearchBinding.inflate(layoutInflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storageRef = FirebaseStorage.getInstance()
        databaseRef = Firebase.database.getReference("videos")
        val options =
            FirebaseRecyclerOptions.Builder<VideoItem>()
                .setQuery(databaseRef, VideoItem::class.java)
                .build()


        searchAdapter = SearchAdapter(options , requireContext())
        binding.searchRecyclerView.adapter = searchAdapter
    }

    override fun onStart() {
        super.onStart()
        searchAdapter.startListening()
    }

    override fun onStop() {
        searchAdapter.stopListening()
        super.onStop()
    }
}