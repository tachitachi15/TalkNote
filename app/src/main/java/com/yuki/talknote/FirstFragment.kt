package com.yuki.talknote

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yuki.talkmemo.Talk
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private lateinit var viewModel:TalkViewModel
    private lateinit var addTalkViewModel: AddTalkViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = TalkListAdapter(context)
        val swipeToDismissTouchHelper = getSwipeToDismissTouchHelper(adapter)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        addTalkViewModel = ViewModelProvider(this).get(AddTalkViewModel::class.java)
        viewModel = ViewModelProvider(this).get(TalkViewModel::class.java)
        viewModel.allTalks.observe(viewLifecycleOwner,{ talks ->
            talks?.let {adapter.setTalks(it)}
        })
        swipeToDismissTouchHelper.attachToRecyclerView(recyclerView)

        val floatingActionButton = view.findViewById<FloatingActionButton>(R.id.fab)
        floatingActionButton.setOnClickListener{
            val date:String = SimpleDateFormat("yyyy-MM-dd").format(Date()).toString()
            addTalkViewModel.insert(Talk(0,date,"test"))
            val intent = Intent(activity,TalkRecordActivity::class.java)
            startActivity(intent)
        }
    }

    //リサイクルビューのスワイプ処理
    private fun getSwipeToDismissTouchHelper(adapter: TalkListAdapter) =
        ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.ACTION_STATE_IDLE,
            ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        ){
            //ドラッグ＆ドロップの処理　今回はなし
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }
            //スワイプ時　消去
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val talks= adapter.talks
                addTalkViewModel.delete(talks[viewHolder.adapterPosition])
            }

            //スワイプしてる時の背景とアイコンの設定
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                val itemView = viewHolder.itemView
                val background = ColorDrawable(Color.RED)
                val deleteIcon = context?.let {
                    AppCompatResources.getDrawable(
                        it,
                        R.drawable.ic_baseline_delete_24
                    )
                }
                val iconMarginVertical =
                    (viewHolder.itemView.height - deleteIcon!!.intrinsicHeight) / 2

                deleteIcon.setBounds(
                    itemView.left + iconMarginVertical,
                    itemView.top + iconMarginVertical,
                    itemView.left + iconMarginVertical + deleteIcon.intrinsicWidth,
                    itemView.bottom - iconMarginVertical
                )
                background.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.right + dX.toInt(),
                    itemView.bottom
                )
                background.draw(c)
                deleteIcon.draw(c)
            }
        })
}
