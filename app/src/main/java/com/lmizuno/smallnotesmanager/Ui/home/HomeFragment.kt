package com.lmizuno.smallnotesmanager.Ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lmizuno.smallnotesmanager.Adapters.CollectionListAdapter
import com.lmizuno.smallnotesmanager.DBManager.AppDatabase
import com.lmizuno.smallnotesmanager.Listeners.CollectionsClickListener
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.NewCollectionActivity
import com.lmizuno.smallnotesmanager.R
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

        db = AppDatabase.getInstance(requireContext())

        val collectionList: List<Collection> = db.collectionDao().getAll()

        updateRecycler(collectionList)

        binding.fabNewCollection.setOnClickListener {
            val intent = Intent(requireContext(), NewCollectionActivity::class.java)
            newCollectionActivityResultLauncher.launch(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        Toast.makeText(requireContext(), "Refreshing", Toast.LENGTH_SHORT).show()
        updateRecycler(db.collectionDao().getAll())
    }

    private fun updateRecycler(collections: List<Collection>) {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = CollectionListAdapter(collections, CollectionsClickListener(this))
        recyclerView.adapter = adapter
    }

    private val newCollectionActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val coll: Collection? =
                    data?.getSerializableExtra("collection", Collection::class.java)
                db.collectionDao().insert(coll!!)

                //List will be updated by onResume
            }
        }
}