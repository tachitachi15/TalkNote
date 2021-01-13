package com.yuki.talknote

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.yuki.talkmemo.Talk

class TalkListAdapter internal constructor(context: Context?) : RecyclerView.Adapter<TalkListAdapter.TalkViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var talks = emptyList<Talk>()

    inner class TalkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val keywordView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TalkViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return TalkViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TalkViewHolder, position: Int) {
        val current = talks[position]
        //holder.dateView.text = current.date
        holder.keywordView.text = current.keywords
        //ここで画面遷移を実装　
        holder.itemView.setOnClickListener(){
            val action = FirstFragmentDirections.actionFirstFragmentToSecondFragment(current)
            NavHostFragment.findNavController(FragmentManager.findFragment(it)).navigate(action)
        }
    }

    internal fun setTalks(talks: List<Talk>) {
        this.talks = talks
        notifyDataSetChanged()
    }

    override fun getItemCount() = talks.size
}