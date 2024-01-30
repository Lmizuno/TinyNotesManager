package com.lmizuno.smallnotesmanager.Ui.collection

import com.lmizuno.smallnotesmanager.R
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lmizuno.smallnotesmanager.Adapters.ItemListAdapter
import com.lmizuno.smallnotesmanager.DBManager.AppDatabase
import com.lmizuno.smallnotesmanager.Listeners.ItemsClickListener
import com.lmizuno.smallnotesmanager.MainActivity
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.Models.Item
import com.lmizuno.smallnotesmanager.EditorItemActivity
import com.lmizuno.smallnotesmanager.databinding.FragmentCollectionViewBinding

class CollectionViewFragment : Fragment() {
    private var _binding: FragmentCollectionViewBinding? = null

    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var activity: MainActivity
    lateinit var currentCollection: Collection
    var editorToggle: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectionViewBinding.inflate(inflater, container, false)
        val root: View = binding.root

        activity = requireActivity() as MainActivity

        recyclerView = binding.recyclerCollection

        db = AppDatabase.getInstance(requireContext())

        currentCollection = arguments?.getSerializable("collection", Collection::class.java)!!

        val itemList: List<Item> =
            db.collectionDao().getCollectionItems(currentCollection.collectionId)

        updateRecycler(itemList)

        binding.fabNewItem.setOnClickListener {
            if (!editorToggle) {
                binding.fabNewItem.setImageResource(R.drawable.baseline_app_settings_alt_24)
                binding.fabNewItem.backgroundTintList =
                    resources.getColorStateList(R.color.yellow_pastel, requireContext().theme)

                editorToggle = !editorToggle
            } else {
                binding.fabNewItem.setImageResource(R.drawable.baseline_app_shortcut_24)

                binding.fabNewItem.backgroundTintList =
                    resources.getColorStateList(R.color.teal_200, requireContext().theme)
                editorToggle = !editorToggle
            }
        }

        binding.bottomAppBar.setOnMenuItemClickListener{
            when (it.itemId) {
                R.id.addItemButton -> {
                    val intent = Intent(requireContext(), EditorItemActivity::class.java)
                    newItemActivityResultLauncher.launch(intent)

                    true
                }

                R.id.shareCollection -> {
                    //TODO: create a way to share as a unique file to be imported later
                    true
                }

                R.id.downloadCollection -> {
                    //TODO: create a way to download as PDF
                    true
                }
                else -> false
            }
        }

        return root
    }



    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun updateRecycler(items: List<Item>) {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = ItemListAdapter(items, ItemsClickListener(this))
        recyclerView.adapter = adapter
    }

    val newItemActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val item: Item? =
                    data?.getSerializableExtra("item", Item::class.java)

                if (item != null) {
                    item.collectionId = currentCollection.collectionId
                }
                db.itemDao().insert(item!!)

                updateRecycler(
                    db.collectionDao().getCollectionItems(currentCollection.collectionId)
                )
            }
        }

    val editItemActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val item: Item? =
                    data?.getSerializableExtra("item", Item::class.java)

                db.itemDao().update(item!!)

                updateRecycler(
                    db.collectionDao().getCollectionItems(currentCollection.collectionId)
                )
            }
        }
}