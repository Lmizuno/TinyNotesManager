package com.lmizuno.smallnotesmanager.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lmizuno.smallnotesmanager.models.Node
import com.lmizuno.smallnotesmanager.models.NodeType
import com.lmizuno.smallnotesmanager.repositories.NodeRepository
import kotlinx.coroutines.launch

class NodeViewModel(application: Application) : AndroidViewModel(application) {
    private val nodeRepository = NodeRepository.getInstance(application)

    private val _nodes = MutableLiveData<List<Node>>()
    val nodes: LiveData<List<Node>> = _nodes

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    companion object {
        private const val TAG = "NodeViewModel"
    }

    /**
     * Loads nodes for the given parent ID
     */
    fun loadNodes(parentId: String?) {
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val results = nodeRepository.queryNodesByParent(parentId)
                _nodes.value = results
                _loading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error loading nodes", e)
                _error.value = e.message
                _loading.value = false
            }
        }
    }

    /**
     * Creates a new folder
     */
    fun createFolder(
        name: String, description: String, parentId: String?, onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val folder = nodeRepository.createFolder(name, description, parentId)

                if (folder != null) {
                    // Refresh the node list
                    loadNodes(parentId)
                    onComplete(true)
                } else {
                    _error.value = "Failed to create folder"
                    onComplete(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating folder", e)
                _error.value = e.message
                onComplete(false)
            }
        }
    }

    /**
     * Creates a new note
     */
    fun createNote(
        name: String, content: String, parentId: String?, onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val note = nodeRepository.createNote(name, content, parentId)

                if (note != null) {
                    // Refresh the node list
                    loadNodes(parentId)
                    onComplete(true)
                } else {
                    _error.value = "Failed to create note"
                    onComplete(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating note", e)
                _error.value = e.message
                onComplete(false)
            }
        }
    }

    /**
     * Updates an existing node
     */
    fun updateNode(node: Node, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = nodeRepository.updateNode(node)

                if (success) {
                    // Refresh the node list
                    loadNodes(node.parentId)
                    onComplete(true)
                } else {
                    _error.value = "Failed to update node"
                    onComplete(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating node", e)
                _error.value = e.message
                onComplete(false)
            }
        }
    }

    /**
     * Deletes a node
     */
    fun deleteNode(nodeId: String, parentId: String?, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = nodeRepository.deleteNode(nodeId)

                if (success) {
                    // Refresh the node list
                    loadNodes(parentId)
                    onComplete(true)
                } else {
                    _error.value = "Failed to delete node"
                    onComplete(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting node", e)
                _error.value = e.message
                onComplete(false)
            }
        }
    }

    /**
     * Clears any error state
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Gets a single node by ID
     */
    fun getNode(nodeId: String, onComplete: (Node?) -> Unit) {
        viewModelScope.launch {
            try {
                val node = nodeRepository.getNode(nodeId)
                onComplete(node)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting node", e)
                _error.value = e.message
                onComplete(null)
            }
        }
    }

    /**
     * Loads nodes recursively for the given parent ID
     */
    fun loadNodesRecursively(parentId: String?, onComplete: (List<Node>) -> Unit) {
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val allNodes = mutableListOf<Node>()

                val currentLevelNodes = nodeRepository.queryNodesByParent(parentId)

                for (node in currentLevelNodes) {
                    allNodes.add(node)
                    if (node.type == NodeType.FOLDER) {
                        val childNodes = nodeRepository.queryNodesRecursively(node.id)
                        allNodes.addAll(childNodes)
                    }
                }

                onComplete(allNodes)
                _loading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error loading nodes recursively", e)
                _error.value = e.message
                _loading.value = false
                onComplete(emptyList())
            }
        }
    }
} 