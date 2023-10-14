package com.godjango.godjangonotify20.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.godjango.godjangonotify20.R
import com.godjango.godjangonotify20.data.models.toMessage
import com.godjango.godjangonotify20.databinding.FragmentMessageBinding
import com.godjango.godjangonotify20.ui.adapters.MessageAdapter
import com.godjango.godjangonotify20.ui.components.ConfirmDialog
import com.godjango.godjangonotify20.ui.viewmodel.MessageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


/**
 * A simple [Fragment] subclass.
 * Use the [MessageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class MessageFragment : Fragment(), CoroutineScope {
    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!
    private val messageViewModel:MessageViewModel by viewModels()

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)

        val llm = LinearLayoutManager(context)
        llm.stackFromEnd = true
        llm.reverseLayout = true
        llm.orientation = LinearLayoutManager.VERTICAL
        binding.rvMsg.layoutManager = llm
        binding.ivArchive.setOnClickListener {
            context?.let { it1 ->
                ConfirmDialog().invoke(it1,R.string.do_you_want_to_save_all_in_the_history){
                    messageViewModel.archiveAll()
                }
            }
        }
        return binding.root
    }
    override fun onStop() {
        job.cancel()
        super.onStop()
    }
    override fun onStart() {
        super.onStart()
        job = SupervisorJob()
        launch {
            messageViewModel.messages
                .collect{
                    if(it.isNotEmpty()) binding.shimmer.visibility = GONE
                    binding.rvMsg.adapter =
                        parentFragment?.context?.let { it1 ->
                            MessageAdapter(it, it1,{it2->
                                messageViewModel.viewed(it2)
                            }){msg->
                                messageViewModel.saveInHistory(msg.id)
                            }
                        }
                }
        }
    }
}