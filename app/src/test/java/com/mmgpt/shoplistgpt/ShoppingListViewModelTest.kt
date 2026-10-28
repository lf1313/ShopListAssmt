package com.mmgpt.shoplistgpt

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShoppingListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ShoppingListViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = ShoppingListViewModel(app)
    }

    @Test
    fun testCategoryReorderingAndIdTracking() {
        val cat1 = ShoppingCategory(1L, "Meat", listOf(ShoppingItem(101L, "Steak")))
        val cat2 = ShoppingCategory(2L, "Dairy", listOf(ShoppingItem(102L, "Milk")))
        val cat3 = ShoppingCategory(3L, "Fruit", listOf(ShoppingItem(103L, "Apple")))

        viewModel.setCategories(listOf(cat1, cat2, cat3))

        // Reorder cat2 (Dairy) from index 1 to index 0
        viewModel.reorderCategory(1, 0)

        val categories = viewModel.categories.value
        assertNotNull(categories)
        assertEquals(3, categories?.size)
        assertEquals(2L, categories?.get(0)?.id)
        assertEquals("Dairy", categories?.get(0)?.heading)
        assertEquals(1L, categories?.get(1)?.id)
        assertEquals("Meat", categories?.get(1)?.heading)
        assertEquals(3L, categories?.get(2)?.id)
        assertEquals("Fruit", categories?.get(2)?.heading)
    }

    @Test
    fun testItemOperationsById() {
        val cat1 = ShoppingCategory(1L, "Meat", listOf(ShoppingItem(101L, "Steak")))
        viewModel.setCategories(listOf(cat1))

        // Add item using categoryId 1L
        viewModel.addItem(1L, 0, "Chicken")

        val categories = viewModel.categories.value
        assertNotNull(categories)
        assertEquals(2, categories?.get(0)?.items?.size)
        assertEquals("Chicken", categories?.get(0)?.items?.get(0)?.name)
    }

    @Test
    fun testRepeatedCategoryReordering() {
        val cat1 = ShoppingCategory(1L, "Meat", emptyList())
        val cat2 = ShoppingCategory(2L, "Dairy", emptyList())
        val cat3 = ShoppingCategory(3L, "Fruit", emptyList())

        viewModel.setCategories(listOf(cat1, cat2, cat3))

        // Reorder repeatedly
        viewModel.reorderCategory(0, 2) // Meat to end: [Dairy, Fruit, Meat]
        viewModel.reorderCategory(2, 0) // Meat back to start: [Meat, Dairy, Fruit]

        val categories = viewModel.categories.value
        assertNotNull(categories)
        assertEquals(1L, categories?.get(0)?.id)
        assertEquals(2L, categories?.get(1)?.id)
        assertEquals(3L, categories?.get(2)?.id)
    }
}
