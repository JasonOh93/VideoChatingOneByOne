package com.jasonoh.videochatingonebyonefromhowlyoutube

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Transformations.map
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.jasonoh.videochatingonebyonefromhowlyoutube.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    var array: MutableList<UserDTO> = arrayListOf()
    var uids: MutableList<String> = arrayListOf()
    val myUid = FirebaseAuth.getInstance().uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        FirebaseFirestore.getInstance().collection("users").get().addOnCompleteListener {
            task->
            array.clear()
            uids.clear()
            for(item in task.result!!.documents){
                if(myUid != item.id){
                    array.add(item.toObject(UserDTO::class.java)!!)
                    uids.add(item.id)
                }
            }
//            println("TEST : $array")
            binding.peopleListRecyclerview.adapter?.notifyDataSetChanged()
        }
        binding.peopleListRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.peopleListRecyclerview.adapter = RecyclerViewAdapter()
        watchingMyUidVideoRequest()
    }

    fun watchingMyUidVideoRequest(){
        FirebaseFirestore.getInstance().collection("users").document(myUid!!).addSnapshotListener { value, error ->
            val userDTO = value?.toObject(UserDTO::class.java)
            if(userDTO?.channel != null){
                showJoinDialog(userDTO.channel!!)
            }
        }
    }
    fun showJoinDialog(channel: String){
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("${channel}방에 참여하시겠습니까?")
            setPositiveButton("Yes"){
                dialogInterface, i ->
                openVideoActivity(channel)
                removeChannelStr()
            }
            setNegativeButton("No"){ dialogInterface, i ->
                dialogInterface.dismiss()
            }
            create()
            show()
        }
    }

    fun removeChannelStr(){
        val map = mutableMapOf<String, Any>()
        map["channel"] = FieldValue.delete()
        FirebaseFirestore.getInstance().collection("users").document(myUid!!).update(map)
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewAdapter.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_person, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
            holder.itemEmail.text = array[position].email
            Glide.with(this@MainActivity).load(R.drawable.img_person_empty).circleCrop().into(holder.ivPeople)
            holder.itemView.setOnClickListener {
                val channelNumber = (1000..1000000).random().toString()
                openVideoActivity(channelNumber)
                createVideoChatRoom(position, channelNumber)
            }
        }

        override fun getItemCount(): Int {
            return array.size
        }
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
            val itemEmail = view.findViewById<TextView>(R.id.item_email)
            val ivPeople = view.findViewById<ImageView>(R.id.imageView)
        }

    }
    fun openVideoActivity(channelId: String){
        val i = Intent(this, VideoActivity::class.java)
        i.putExtra("channelId", channelId)
        startActivity(i)
    }

    fun createVideoChatRoom(position: Int, channel : String){
        val map = mutableMapOf<String, Any>()
        map["channel"] = channel
        FirebaseFirestore.getInstance().collection("users").document(uids[position]).update(map)
    }
}