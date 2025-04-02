package com.lmizuno.smallnotesmanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.lmizuno.smallnotesmanager.adapters.NodesPresentationAdapter
import com.lmizuno.smallnotesmanager.databinding.ActivityNotePresentationBinding
import com.lmizuno.smallnotesmanager.models.Note
import com.lmizuno.smallnotesmanager.viewmodels.NodeViewModel
import io.noties.markwon.Markwon
import me.relex.circleindicator.CircleIndicator3

enum class PRESENTATION_OPTION {
    RECURSIVE, SHALLOW
}

class NodePresentationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotePresentationBinding
    private lateinit var viewModel: NodeViewModel
    private lateinit var markwon: Markwon
    private lateinit var viewpager: ViewPager2
    private lateinit var indicator: CircleIndicator3

    private var currentFolderId: String? = null
    private var presentationOption: Int? = 0
    private var notes: List<Note> = emptyList()

    companion object {
        const val EXTRA_FOLDER_ID = "folder_id"
        const val EXTRA_PRESENTATION_OPTION = "presentation_option"

        fun createIntent(
            context: Context,
            folderId: String?,
            presOption: PRESENTATION_OPTION = PRESENTATION_OPTION.SHALLOW
        ): Intent {
            return Intent(context, NodePresentationActivity::class.java).apply {
                putExtra(EXTRA_FOLDER_ID, folderId)
                putExtra(EXTRA_PRESENTATION_OPTION, presOption.ordinal)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotePresentationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[NodeViewModel::class.java]

        // Initialize Markwon for Markdown rendering
        markwon = Markwon.create(this)

        // Get folder ID from intent
        currentFolderId = intent.getStringExtra(EXTRA_FOLDER_ID)
        val presOptionOrdinal =
            intent.getIntExtra(EXTRA_PRESENTATION_OPTION, PRESENTATION_OPTION.SHALLOW.ordinal)
        val presOption = PRESENTATION_OPTION.values()[presOptionOrdinal]

        // Setup UI elements
        viewpager = findViewById(R.id.note_view)
        indicator = findViewById(R.id.page_indicator)
        indicator.setViewPager(viewpager)

        // Load notes based on presentation option
        when (presOption) {
            PRESENTATION_OPTION.RECURSIVE -> loadNotesRecursively()
            PRESENTATION_OPTION.SHALLOW -> loadNotes()
        }
    }

    private fun loadNotes() {
        viewModel.loadNodes(currentFolderId)

        // Observe nodes LiveData
        viewModel.nodes.observe(this) { nodes ->
            // Filter to only include notes
            notes = nodes.filterIsInstance<Note>()

            if (notes.isNotEmpty()) {
                viewpager.adapter = NodesPresentationAdapter(notes, markwon)
            }
        }

        // Observe errors
        viewModel.error.observe(this) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun loadNotesRecursively() {
        viewModel.loadNodesRecursively(currentFolderId) { allNodes ->
            // Filter to only include notes
            notes = allNodes.filterIsInstance<Note>()

            if (notes.isNotEmpty()) {
                viewpager.adapter = NodesPresentationAdapter(notes, markwon)
                indicator.setViewPager(viewpager)
            } else {
                Toast.makeText(this, getString(R.string.no_notes_to_display), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}