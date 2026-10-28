package com.mmgpt.shoplistgpt

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.recyclerview.widget.RecyclerView

class CategoryPagerAdapter(
    activity: FragmentActivity,
    private val viewModel: ShoppingListViewModel,
    private val planMode: Boolean = false
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int =
        (viewModel.categories.value?.size ?: 0) + 1

    override fun getItemId(position: Int): Long {
        return if (planMode) {
            if (position == 0) -1L
            else viewModel.categories.value?.getOrNull(position - 1)?.id ?: RecyclerView.NO_ID
        } else {
            val size = viewModel.categories.value?.size ?: 0
            if (position == size) -2L
            else viewModel.categories.value?.getOrNull(position)?.id ?: RecyclerView.NO_ID
        }
    }

    override fun containsItem(itemId: Long): Boolean {
        if (itemId == -1L || itemId == -2L) return true
        return viewModel.categories.value?.any { it.id == itemId } == true
    }

    override fun createFragment(position: Int): Fragment =
        if (planMode) {
            if (position == 0) {
                PlanCategoriesFragment()
            } else {
                val categoryId = viewModel.categories.value?.getOrNull(position - 1)?.id ?: -1L
                PlanCategoryFragment.newInstance(categoryId)
            }
        } else {
            val size = viewModel.categories.value?.size ?: 0
            if (position == size) {
                ShopCategoriesFragment()
            } else {
                val categoryId = viewModel.categories.value?.getOrNull(position)?.id ?: -1L
                CategoryFragment.newInstance(categoryId)
            }
        }
}
