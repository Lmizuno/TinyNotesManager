package com.lmizuno.smallnotesmanager.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lmizuno.smallnotesmanager.EditorCollectionActivity
import com.lmizuno.smallnotesmanager.R
import com.lmizuno.smallnotesmanager.adapters.CollectionListAdapter
import com.lmizuno.smallnotesmanager.databinding.FragmentHomeBinding
import com.lmizuno.smallnotesmanager.dbManager.AppDatabase
import com.lmizuno.smallnotesmanager.listeners.CollectionsClickListener
import com.lmizuno.smallnotesmanager.models.Collection
import com.lmizuno.smallnotesmanager.scripts.DeprecationManager
import com.lmizuno.smallnotesmanager.scripts.Sharing

// Since this is a legacy code, I won't be maintaining it for now.
@SuppressLint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)  // Enable options menu
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController().navigate(R.id.action_navigation_home_to_settingsFragment)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerView = binding.recyclerHome

        db = AppDatabase.getInstance(requireContext())

        val collectionList: List<Collection> = db.collectionDao().getAll()

        updateRecycler(collectionList)

        binding.fabNewCollection.setOnClickListener {
            val intent = Intent(requireContext(), EditorCollectionActivity::class.java)
            intent.putExtra("intent", "add")
            newCollectionActivityResultLauncher.launch(intent)
        }

        binding.bottomAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.importCollection -> {
                    val intent =
                        Intent().setType("application/json").setAction(Intent.ACTION_GET_CONTENT)
                    fileChooserActivity.launch(
                        Intent.createChooser(
                            intent, getString(R.string.select_file)
                        )
                    ) //"Select a file"

                    true
                }

                else -> false
            }
        }

        activity?.title = getString(R.string.title_home)

        return root
    }

    private val fileChooserActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val selectedFileUri: Uri? = it.data?.data
                if (selectedFileUri != null) {

                    Sharing().importFromFile(selectedFileUri, requireContext())
                }
            }
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
                val intent: Intent? = result.data

                if (intent?.hasExtra("collection") == true) {
                    val coll: Collection = DeprecationManager().getSerializable(
                        intent, "collection", Collection::class.java
                    )
                    db.collectionDao().insert(coll)
                }

                //List will be updated by onResume
            }
        }
}