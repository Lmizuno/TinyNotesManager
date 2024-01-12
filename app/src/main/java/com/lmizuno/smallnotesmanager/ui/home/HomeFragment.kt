package com.lmizuno.smallnotesmanager.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Ignore
import com.lmizuno.smallnotesmanager.Adapters.CollectionListAdapter
import com.lmizuno.smallnotesmanager.listeners.CollectionClickListener
import com.lmizuno.smallnotesmanager.DBManager.AppDatabase
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.NewCollectionActivity
import com.lmizuno.smallnotesmanager.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerView = binding.recyclerHome

        //Instantiate DB
        db = AppDatabase.getInstance(requireContext())

        //Get Collections
        val collectionList: List<Collection> = db.collectionDao().getAll()

        updateRecycler(collectionList)

        binding.fabNewCollection.setOnClickListener{
            val intent = Intent(requireContext(), NewCollectionActivity::class.java)
            someActivityResultLauncher.launch(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val someActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result here, e.g., extract data from the Intent
                val data: Intent? = result.data
                // Process the data as needed
                val coll: Collection? =  data?.getSerializableExtra("collection", Collection::class.java)
                db.collectionDao().insert(coll!!)

                updateRecycler(db.collectionDao().getAll())
            }
        }

    private fun updateRecycler(collections: List<Collection>) {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = CollectionListAdapter(collections, CollectionsClickListener)
        recyclerView.adapter = adapter
    }

    companion object CollectionsClickListener : CollectionClickListener {
        override fun onClick(collection: Collection) {
            //TODO
        }

        override fun onLongClick(collection: Collection, cardView: CardView) {
            //TODO
        }
    }
}