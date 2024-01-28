package com.lmizuno.smallnotesmanager.Ui.collection

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
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.Models.Item
import com.lmizuno.smallnotesmanager.databinding.FragmentCollectionViewBinding
import com.lmizuno.smallnotesmanager.Listeners.ItemsClickListener
import com.lmizuno.smallnotesmanager.MainActivity
import com.lmizuno.smallnotesmanager.NewItemActivity

class CollectionViewFragment : Fragment() {
    private var _binding: FragmentCollectionViewBinding? = null

    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var activity: MainActivity
    private lateinit var currentCollection: Collection
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
            val intent = Intent(requireContext(), NewItemActivity::class.java)
            newItemActivityResultLauncher.launch(intent)
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
        val adapter = ItemListAdapter(items, ItemsClickListener())
        recyclerView.adapter = adapter
    }

    private val newItemActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val item: Item? =
                    data?.getSerializableExtra("item", Item::class.java)
                db.itemDao().insert(item!!)

                updateRecycler(
                    db.collectionDao().getCollectionItems(currentCollection.collectionId)
                )
            }
        }
}