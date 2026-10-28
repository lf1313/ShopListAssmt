package com.mmgpt.shoplistgpt

import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GestureDetectorCompat
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.mmgpt.shoplistgpt.adapter.ItemAdapter
import com.mmgpt.shoplistgpt.databinding.FragmentCategoryBinding
import com.mmgpt.shoplistgpt.ShoppingItem

class PlanCategoriesFragment : Fragment() {

    private val viewModel: ShoppingListViewModel by activityViewModels()
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        lateinit var adapter: ItemAdapter
        val dragHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                val itemFrom = adapter.currentList.getOrNull(from) ?: return false
                val itemTo = adapter.currentList.getOrNull(to) ?: return false
                if (itemFrom.isPlaceholder || itemTo.isPlaceholder) return false
                val currentCategories = viewModel.categories.value ?: return false
                val fromIndex = currentCategories.indexOfFirst { it.id == itemFrom.id }
                val toIndex = currentCategories.indexOfFirst { it.id == itemTo.id }
                if (fromIndex != -1 && toIndex != -1) {
                    viewModel.reorderCategory(fromIndex, toIndex)
                }
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun isLongPressDragEnabled(): Boolean = false
        })

        adapter = ItemAdapter(
            onAction = { item, action ->
                val categories = viewModel.categories.value ?: emptyList()
                val index = categories.indexOfFirst { it.id == item.id }
                when (action) {
                    ItemAdapter.Action.EDIT -> showEditDialog(index, item.name)
                    ItemAdapter.Action.DELETE -> viewModel.deleteCategory(index)
                    else -> {}
                }
            },
            mode = ItemAdapter.Mode.PLAN,
            dragStart = { vh -> dragHelper.startDrag(vh) },
            onAdd = { position, name ->
                viewModel.addCategory(position - 1, name)
            }
        )
        binding.recyclerView.adapter = adapter
        dragHelper.attachToRecyclerView(binding.recyclerView)

        val gestureDetector = GestureDetectorCompat(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val child = binding.recyclerView.findChildViewUnder(e.x, e.y)
                val position = child?.let { binding.recyclerView.getChildAdapterPosition(it) } ?: RecyclerView.NO_POSITION
                if (position != RecyclerView.NO_POSITION) {
                    val item = adapter.currentList.getOrNull(position)
                    if (item?.isHeading == true) {
                        val categories = viewModel.categories.value ?: emptyList()
                        val catIndex = categories.indexOfFirst { it.id == item.id }
                        if (catIndex != -1) {
                            val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
                            viewPager.currentItem = catIndex + 1
                            return true
                        }
                    }
                }
                return super.onDoubleTap(e)
            }
        })

        binding.recyclerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }

        binding.btnClearAll.visibility = View.VISIBLE
        binding.btnClearAll.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.confirm_clear_all))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    viewModel.clearAllCategories()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        viewModel.categories.observe(viewLifecycleOwner) { list ->
            binding.tvHeading.text = getString(R.string.categories_heading)
            binding.tvCount.text = ""
            val items = mutableListOf<ShoppingItem>()
            items.add(ShoppingItem(-1, "", isPlaceholder = true))
            list.forEach { cat ->
                items.add(ShoppingItem(cat.id, cat.heading, isHeading = true))
            }
            items.add(ShoppingItem(-2, "", isPlaceholder = true))
            adapter.submitList(items)
        }
    }

    private fun showEditDialog(index: Int, current: String) {
        requireContext().showEditInputDialog(getString(R.string.edit), current) { text ->
            viewModel.editCategory(index, text)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

