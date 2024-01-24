package com.example.todolist.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.EachTodoItemBinding

class TodoAdapter(private val list: MutableList<TodoData>):
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    private var listener: TodoAdapterClicksInterface? = null
    fun setListener(listener:TodoAdapterClicksInterface){
        this.listener = listener
    }
    inner class TodoViewHolder(val binding:EachTodoItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = EachTodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        with(holder){
            with(list[position]){
                binding.todoTask.text = this.task

                binding.deleteTask.setOnClickListener {
                    listener?.onDeleteTaskBtnClicked(this)
                }
                binding.editTask.setOnClickListener {
                    listener?.onEditTaskBtnClicked(this)
                }
            }
        }
        }
    interface TodoAdapterClicksInterface {
        fun onDeleteTaskBtnClicked(todoData: TodoData)
        fun onEditTaskBtnClicked(todoData: TodoData)
    }
}