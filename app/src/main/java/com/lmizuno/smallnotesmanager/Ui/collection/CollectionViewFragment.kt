package com.lmizuno.smallnotesmanager.Ui.collection

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.lmizuno.smallnotesmanager.Adapters.ItemListAdapter
import com.lmizuno.smallnotesmanager.Adapters.ItemMoveCallback
import com.lmizuno.smallnotesmanager.DBManager.AppDatabase
import com.lmizuno.smallnotesmanager.EditorCollectionActivity
import com.lmizuno.smallnotesmanager.EditorItemActivity
import com.lmizuno.smallnotesmanager.Listeners.ItemsClickListener
import com.lmizuno.smallnotesmanager.MainActivity
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.Models.Item
import com.lmizuno.smallnotesmanager.R
import com.lmizuno.smallnotesmanager.Scripts.DeprecationManager
import com.lmizuno.smallnotesmanager.Scripts.Sharing
import com.lmizuno.smallnotesmanager.Ui.dialogs.DeleteDialogFragment
import com.lmizuno.smallnotesmanager.databinding.FragmentCollectionViewBinding
import java.io.File

class CollectionViewFragment : Fragment() {
    private var _binding: FragmentCollectionViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemListAdapter
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
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = ItemListAdapter(ArrayList(), ItemsClickListener(this))
        recyclerView.adapter = adapter

        val callback: ItemTouchHelper.Callback = ItemMoveCallback(adapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)

        db = AppDatabase.getInstance(requireContext())

        currentCollection =
            arguments?.let {
                DeprecationManager().getSerializable(
                    it,
                    "collection",
                    Collection::class.java
                )
            }!!

        activity.title = currentCollection.name

        binding.speedDialCollectionViewMenu.apply {
            mainFabClosedBackgroundColor = ResourcesCompat.getColor(resources, R.color.primary, requireActivity().theme)
            mainFabClosedIconColor = ResourcesCompat.getColor(resources, R.color.white, requireActivity().theme)
            
            addActionItem(
                SpeedDialActionItem.Builder(R.id.addItemButton, R.drawable.baseline_add_24_white)
                    .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary, requireActivity().theme))
                    .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, requireActivity().theme))
                    .setLabel("Add Item")
                    .setLabelColor(Color.WHITE)
                    .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary, requireActivity().theme))
                    .create()
            )

            addActionItem(
                SpeedDialActionItem.Builder(R.id.editCollection, R.drawable.baseline_edit_24)
                    .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.yellow_pastel, requireActivity().theme))
                    .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, requireActivity().theme))
                    .setLabel("Edit Mode")
                    .setLabelColor(Color.WHITE)
                    .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary, requireActivity().theme))
                    .create()
            )

            addActionItem(
                SpeedDialActionItem.Builder(R.id.shareCollection, R.drawable.baseline_share_white_24)
                    .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.teal_200, requireActivity().theme))
                    .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, requireActivity().theme))
                    .setLabel("Share Collection")
                    .setLabelColor(Color.WHITE)
                    .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary, requireActivity().theme))
                    .create()
            )

            addActionItem(
                SpeedDialActionItem.Builder(R.id.deleteCollection, R.drawable.recycle_bin_icon)
                    .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.red, requireActivity().theme))
                    .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, requireActivity().theme))
                    .setLabel("Delete Collection")
                    .setLabelColor(Color.WHITE)
                    .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary, requireActivity().theme))
                    .create()
            )

            setOnActionSelectedListener { actionItem ->
                when (actionItem.id) {
                    R.id.addItemButton -> {
                        val intent = Intent(requireContext(), EditorItemActivity::class.java)
                        intent.putExtra("intent", "add")
                        editorItemActivityResultLauncher.launch(intent)
                        close() // Closes the Speed Dial
                        true // Returns true to keep the Speed Dial open
                    }
                    R.id.editCollection -> {
                        removeActionItemById(R.id.editCollection)
                        if(!editorToggle){
                            addActionItem(
                                SpeedDialActionItem.Builder(R.id.editCollection, R.drawable.baseline_edit_off_24)
                                    .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary, requireActivity().theme))
                                    .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, requireActivity().theme))
                                    .setLabel("View Mode")
                                    .setLabelColor(Color.WHITE)
                                    .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary, requireActivity().theme))
                                    .create(), 1
                            )
                        }else{
                            addActionItem(
                                SpeedDialActionItem.Builder(R.id.editCollection, R.drawable.baseline_edit_24)
                                    .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.yellow_pastel, requireActivity().theme))
                                    .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.white, requireActivity().theme))
                                    .setLabel("Edit Mode")
                                    .setLabelColor(Color.WHITE)
                                    .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.yellow_pastel, requireActivity().theme))
                                    .create(), 1
                            )
                        }

                        editorToggle = !editorToggle
                        adapter.setEditorToggle(editorToggle)
                        close()
                        false
                    }
                    R.id.deleteCollection -> {
                        //Open Dialog
                        DeleteDialogFragment("Do you want to delete ${currentCollection.name}?", {
                            // Delete
                            Toast.makeText(
                                requireContext(),
                                "Deleting ${currentCollection.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                            db.collectionDao().deleteCollectionAndItems(currentCollection)

                            activity.supportFragmentManager.popBackStack()
                        }, {
                            // Cancel
                        }).show(activity.supportFragmentManager, "DELETE_DIALOG")

                        true
                    }
                    R.id.shareCollection -> {
                        val file: File = Sharing().saveToFile(currentCollection, requireContext())

                        val uri: Uri = FileProvider.getUriForFile(
                            requireContext(),
                            "com.smallnotesmanager.fileprovider",
                            file,
                        )

                        val shareIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, uri)
                            type = "text/*"
                        }

                        startActivity(
                            Intent.createChooser(
                                shareIntent,
                                "Share ${currentCollection.name}"
                            )
                        )

                        close()
                        true
                    }
                    else -> false
                }
            }
        }

        return root
    }

    override fun onResume() {
        super.onResume()

        updateAdapter(db.collectionDao().getCollectionItems(currentCollection.collectionId))
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun updateAdapter(items: List<Item>) {
        val array = arrayListOf<Item>()
        array.addAll(items)

        adapter.itemList = array
        adapter.notifyDataSetChanged()
    }

    val editorCollectionActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val collection: Collection? =
                    data?.let {
                        DeprecationManager().getSerializable(
                            it,
                            "collection",
                            Collection::class.java
                        )
                    }
                val intent: String? = data?.getStringExtra("intent")

                if (intent == "update") {
                    db.collectionDao().update(collection!!)
                    activity.title = collection.name
                }
            }
        }

    val editorItemActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val item: Item? =
                    data?.let { DeprecationManager().getSerializable(it, "item", Item::class.java) }
                val intent: String? = data?.getStringExtra("intent")

                if (intent == "update") {
                    db.itemDao().update(item!!)
                } else {
                    if (item != null) {
                        item.collectionId = currentCollection.collectionId

                        val sizeOfCollection =
                            db.collectionDao().getCollectionSize(currentCollection.collectionId)
                        item.orderN = sizeOfCollection + 1

                        db.itemDao().insert(item)
                    }
                }
            }
        }
}
