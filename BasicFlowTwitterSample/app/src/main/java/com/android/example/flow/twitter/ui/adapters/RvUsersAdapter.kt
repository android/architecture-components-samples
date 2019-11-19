package com.android.example.flow.twitter.ui.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.example.flow.twitter.R
import com.android.example.flow.twitter.data.models.User
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


/**
 * Created by Santanu 😁 on 2019-11-19.
 */
class RvUsersAdapter(private var _activity: Activity) :
    RecyclerView.Adapter<RvUsersAdapter.UsersViewHolder>() {
    private val _usersList: MutableList<User> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder =
        UsersViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_user,
                parent,
                false
            )
        )

    override fun getItemCount(): Int =
        _usersList.size

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.bind(_usersList[position])
    }

    /**
     * @param apiList is the list of users received from the API
     */
    fun updateUserList(apiList: List<User>) {
        _usersList.apply {
            clear()
            addAll(apiList)
            notifyDataSetChanged()
        }
    }

    inner class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val _ivUser = itemView.findViewById<View>(R.id.ivUser) as ImageView
        private val _tvUserName = itemView.findViewById<View>(R.id.tvUserName) as TextView
        private val _tvFollowersCount = itemView.findViewById<View>(R.id.tvFollowers) as TextView

        fun bind(user: User) {
            _tvUserName.text = user.name + "(${user.screenName})"
            _tvFollowersCount.text = "Followers - ${user.followersCount}"
            val options = RequestOptions()
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)
            Glide.with(_activity)
                .load(user.profileImage)
                .apply(options)
                .into(_ivUser)
        }
    }
}