package com.Ktoledo.pissdrunxking

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MissionsPagerAdapter(
    private val context: Context,
    private val onAcceptMissionClick: (PDKspot) -> Unit,
    private val onSubmitEvidenceClick: (PDKspot) -> Unit,
    private val onCancelMissionClick: (PDKspot) -> Unit
) : RecyclerView.Adapter<MissionsPagerAdapter.PageViewHolder>() {

    private var availableMissions: MutableList<PDKspot> = mutableListOf()
    private var acceptedMissions: MutableList<PDKspot> = mutableListOf()
    private var completedMissions: MutableList<PDKspot> = mutableListOf()

    fun updateData(available: List<PDKspot>, accepted: List<PDKspot>, completed: List<PDKspot>) {
        availableMissions.clear()
        availableMissions.addAll(available)
        
        acceptedMissions.clear()
        acceptedMissions.addAll(accepted)
        
        completedMissions.clear()
        completedMissions.addAll(completed)
        
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_mission_page, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.recyclerView.layoutManager = LinearLayoutManager(context)
        
        if (position < 3) {
            val adapter = MissionsAdapter(
                context,
                mutableListOf(),
                onAcceptMissionClick,
                onSubmitEvidenceClick,
                onCancelMissionClick
            )
            holder.recyclerView.adapter = adapter

            when (position) {
                0 -> {
                    adapter.updateMissions(availableMissions, MissionsListActivity.MissionType.AVAILABLE)
                    holder.emptyStateContainer.visibility = if (availableMissions.isEmpty()) View.VISIBLE else View.GONE
                }
                1 -> {
                    adapter.updateMissions(acceptedMissions, MissionsListActivity.MissionType.ACCEPTED)
                    holder.emptyStateContainer.visibility = if (acceptedMissions.isEmpty()) View.VISIBLE else View.GONE
                }
                2 -> {
                    adapter.updateMissions(completedMissions, MissionsListActivity.MissionType.COMPLETED)
                    holder.emptyStateContainer.visibility = if (completedMissions.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        } else {
            // Position 3 is Kings Semanales
            val kings = PDKKingsManager.getMockKingsSemanales()
            val kingsAdapter = KingsAdapter(kings)
            holder.recyclerView.adapter = kingsAdapter
            holder.emptyStateContainer.visibility = if (kings.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun getItemCount(): Int {
        return 4
    }

    class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.rvMissions)
        val emptyStateContainer: FrameLayout = itemView.findViewById(R.id.emptyStateContainer)
    }
}
