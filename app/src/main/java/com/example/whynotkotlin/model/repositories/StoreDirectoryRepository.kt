package com.example.whynotkotlin.model.repositories

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

data class DirectoryStore(
    val id: String, val name: String, val address: String, val categories: List<String>,
    val image: String, val website: String, val latitude: Double?, val longitude: Double?
)

class DirectoryIndex(stores: List<DirectoryStore>) {
    val all = stores.sortedBy { it.name.lowercase() }
    val byId = all.associateBy { it.id }
    val byCategory = buildMap<String, MutableList<DirectoryStore>> {
        for (store in all) {
            for (category in store.categories.distinct()) {
                getOrPut(category) { mutableListOf() }.add(store)
            }
        }
    }.toSortedMap()
}

data class DirectorySnapshot(val index: DirectoryIndex, val fromDisk: Boolean)

class StoreDirectoryRepository(private val db: FirebaseFirestore) {
    fun observe() = callbackFlow<QuerySnapshot> {
        val registration = db.collection("stores").addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) close(error)
            else if (snapshot != null) trySend(snapshot)
        }
        awaitClose { registration.remove() }
    }.conflate().map { snapshot ->
        DirectorySnapshot(
            DirectoryIndex(snapshot.documents.mapNotNull(::parseStore)),
            snapshot.metadata.isFromCache
        )
    }.flowOn(Dispatchers.Default)

    private fun parseStore(doc: DocumentSnapshot): DirectoryStore? {
        val name = (doc.get("name") as? String)?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return DirectoryStore(
            doc.id, name, doc.get("address") as? String ?: "",
            (doc.get("categoryIds") as? List<*>)?.filterIsInstance<String>().orEmpty(),
            doc.get("imageUrl") as? String ?: "", doc.get("websiteUrl") as? String ?: "",
            (doc.get("latitude") as? Number)?.toDouble()?.takeIf { it in -90.0..90.0 },
            (doc.get("longitude") as? Number)?.toDouble()?.takeIf { it in -180.0..180.0 }
        )
    }
}
