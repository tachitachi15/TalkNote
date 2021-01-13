package com.yuki.talknote

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        viewModel = ViewModelProvider(this).get(TalkViewModel::class.java)
        viewModel.allTalks.observe(viewLifecycleOwner,{ talks ->
            talks?.let {adapter.setTalks(it)}
        })

        val floatingActionButton = view.findViewById<FloatingActionButton>(R.id.fab)
        floatingActionButton.setOnClickListener{
//            addTalkViewModel = ViewModelProvider(this).get(AddTalkViewModel::class.java)
//            val date:String = SimpleDateFormat("yyyy-MM-dd").format(Date()).toString()
//            addTalkViewModel.insert(Talk(0,date,"test"))
            val intent = Intent(activity,TalkRecordActivity::class.java)
            startActivity(intent)
        }
    }
}