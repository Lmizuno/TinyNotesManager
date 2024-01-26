package com.lmizuno.smallnotesmanager.Ui.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lmizuno.smallnotesmanager.Adapters.ItemListAdapter
import com.lmizuno.smallnotesmanager.DBManager.AppDatabase
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.Models.Item
import com.lmizuno.smallnotesmanager.databinding.FragmentCollectionViewBinding
import com.lmizuno.smallnotesmanager.Listeners.ItemClickListener
import com.lmizuno.smallnotesmanager.MainActivity

class CollectionViewFragment : Fragment() {
    private var _binding: FragmentCollectionViewBinding? = null

    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var activity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectionViewBinding.inflate(inflater, container, false)
        val root: View = binding.root

        activity = requireActivity() as MainActivity
        activity.toggleNavBar(false)

        recyclerView = binding.recyclerCollection

        db = AppDatabase.getInstance(requireContext())

        val collection = arguments?.getSerializable("collection", Collection::class.java)

        if (collection != null) {
            val itemList: List<Item> =
                db.collectionDao().getCollectionItems(collection.collectionId)

            updateRecycler(itemList)
        }

        binding.fabNewItem.setOnClickListener {
        }

        return root
    }

    private fun updateRecycler(items: List<Item>) {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = ItemListAdapter(items, ItemsClickListener)
        recyclerView.adapter = adapter
    }

    companion object ItemsClickListener : ItemClickListener {
        override fun onClick(item: Item) {
            TODO("Not yet implemented")
        }

        override fun onLongClick(item: Item, cardView: CardView) {
            TODO("Not yet implemented")
        }
    }

    override fun onDestroyView() {
        activity.toggleNavBar(true)
        super.onDestroyView()
    }
}