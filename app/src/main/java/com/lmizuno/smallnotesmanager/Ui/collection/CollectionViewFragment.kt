package com.lmizuno.smallnotesmanager.Ui.collection

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lmizuno.smallnotesmanager.Adapters.ItemListAdapter
import com.lmizuno.smallnotesmanager.DBManager.AppDatabase
import com.lmizuno.smallnotesmanager.EditorItemActivity
import com.lmizuno.smallnotesmanager.Listeners.ItemsClickListener
import com.lmizuno.smallnotesmanager.MainActivity
import com.lmizuno.smallnotesmanager.Models.Collection
import com.lmizuno.smallnotesmanager.Models.Item
import com.lmizuno.smallnotesmanager.R
import com.lmizuno.smallnotesmanager.Scripts.DeprecationManager
import com.lmizuno.smallnotesmanager.databinding.FragmentCollectionViewBinding
import com.lmizuno.smallnotesmanager.Scripts.Sharing
import com.lmizuno.smallnotesmanager.Ui.dialogs.DeleteDialogFragment
import java.io.File

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

        currentCollection =
            arguments?.let {
                DeprecationManager().getSerializable(
                    it,
                    "collection",
                    Collection::class.java
                )
            }!!

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

        binding.bottomAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.addItemButton -> {
                    val intent = Intent(requireContext(), EditorItemActivity::class.java)
                    editorItemActivityResultLauncher.launch(intent)

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
                        type = "application/octet-stream"
                    }

                    startActivity(
                        Intent.createChooser(
                            shareIntent,
                            "Share ${currentCollection.name}"
                        )
                    )

                    true
                }

//                R.id.downloadCollection -> {
//                    //TODO: create a way to download as PDF
//                    true
//                }

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

                else -> false
            }
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        updateRecycler(db.collectionDao().getCollectionItems(currentCollection.collectionId))
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

    val editorItemActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val item: Item? =
                    data?.let { DeprecationManager().getSerializable(it, "item", Item::class.java) }

                //TODO: this conflicts when trying to add with the button toggled to edit mode
                if (editorToggle) {
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