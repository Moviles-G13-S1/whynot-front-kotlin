package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.repositories.DirectoryIndex
import com.example.whynotkotlin.model.repositories.DirectoryStore
import org.junit.Assert.assertEquals
import org.junit.Test

class DirectoryIndexTest {
    private fun store(id: String, vararg categories: String) =
        DirectoryStore(id, id, "", categories.toList(), "", "", null, null)

    @Test fun multiCategoryStoreAppearsOncePerCategoryInNameOrder() {
        val z = store("Zara", "fashion", "fashion", "gifts")
        val a = store("Alpha", "fashion")
        val other = store("Other")
        val index = DirectoryIndex(listOf(z, other, a))
        assertEquals(listOf(a, other, z), index.all)
        assertEquals(listOf(a, z), index.byCategory["fashion"])
        assertEquals(listOf(z), index.byCategory["gifts"])
        assertEquals(other, index.byId["Other"])
    }
}
