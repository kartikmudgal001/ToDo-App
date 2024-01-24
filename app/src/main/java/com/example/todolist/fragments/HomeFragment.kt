package com.example.todolist.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolist.databinding.FragmentHomeBinding
import com.example.todolist.utils.TodoAdapter
import com.example.todolist.utils.TodoData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment(), AddTodoPopupFragment.DialogNextBtnClickListener,
    TodoAdapter.TodoAdapterClicksInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentHomeBinding
    private var popUpFragment: AddTodoPopupFragment?= null
    private lateinit var adapter: TodoAdapter
    private lateinit var mList: MutableList<TodoData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getDataFromFirebase()
        registerEvents()
    }
    private fun init(view: View){
        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference
            .child("Tasks").child(auth.currentUser?.uid.toString())

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()
        adapter = TodoAdapter(mList)
        adapter.setListener(this)
        binding.recyclerView.adapter = adapter
    }
    private fun getDataFromFirebase(){
         databaseRef.addValueEventListener(object: ValueEventListener{
             override fun onDataChange(snapshot: DataSnapshot) {
                 mList.clear()
                 for(taskSnapshot in snapshot.children){
                     val todoTask = taskSnapshot.key?.let {
                         TodoData(it, taskSnapshot.value.toString())
                     }
                     if(todoTask!=null){
                         mList.add(todoTask)
                     }
                 }
                 adapter.notifyDataSetChanged()
             }

             override fun onCancelled(error: DatabaseError) {
                 Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
             }

         })
    }
    private fun registerEvents(){
        binding.addBtnHome.setOnClickListener {
            if(popUpFragment!=null)
                childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
            popUpFragment = AddTodoPopupFragment()
            popUpFragment!!.setListener(this)
            popUpFragment!!.show(
                childFragmentManager,
                AddTodoPopupFragment.TAG
            )
        }
    }

    override fun onSaveTask(todo: String, todoEt: TextInputEditText) {
        databaseRef.push().setValue(todo).addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context, "Saved successfully", Toast.LENGTH_SHORT).show()

            }
            else{
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
            todoEt.text = null
            popUpFragment!!.dismiss()
        }
    }

    override fun onUpdateTask(todoData: TodoData, todoEt: TextInputEditText) {
        val map = HashMap<String, Any>()
        map[todoData.taskId] = todoData.task //update key taskId  with value task
        databaseRef.updateChildren(map).addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show()

            }
            else{
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
            todoEt.text = null
            popUpFragment!!.dismiss()
        }
    }

    override fun onDeleteTaskBtnClicked(todoData: TodoData) {
        databaseRef.child(todoData.taskId).removeValue().addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onEditTaskBtnClicked(todoData: TodoData) {
        if(popUpFragment!=null)
            childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
        popUpFragment = AddTodoPopupFragment.newInstance(todoData.taskId, todoData.task)
        popUpFragment!!.setListener(this)
        popUpFragment!!.show(childFragmentManager, AddTodoPopupFragment.TAG)
    }
}