package com.Ktoledo.pissdrunxking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class KingsAdapter(private var kings: List<PDKKing>) : RecyclerView.Adapter<KingsAdapter.KingViewHolder>() {

    fun updateKings(newKings: List<PDKKing>) {
        this.kings = newKings
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_king, parent, false)
        return KingViewHolder(view)
    }

    override fun onBindViewHolder(holder: KingViewHolder, position: Int) {
        val king = kings[position]
        holder.tvRank.text = king.rank.toString()
        holder.tvName.text = king.name
        holder.tvLikes.text = king.likes.toString()
    }

    override fun getItemCount(): Int {
        return kings.size
    }

    class KingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank: TextView = itemView.findViewById(R.id.tvKingRank)
        val tvName: TextView = itemView.findViewById(R.id.tvKingName)
        val tvLikes: TextView = itemView.findViewById(R.id.tvKingLikes)
    }
}
