package com.godjango.godjangonotify20.ui

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.godjango.godjangonotify20.R
import com.godjango.godjangonotify20.databinding.FragmentHistoryBinding
import com.godjango.godjangonotify20.databinding.FragmentMessageBinding
import com.godjango.godjangonotify20.ui.adapters.HistoryAdapter
import com.godjango.godjangonotify20.ui.components.ConfirmDialog
import com.godjango.godjangonotify20.ui.viewmodel.HistoryViewModel
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


/**
 * A simple [Fragment] subclass.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class HistoryFragment : Fragment(),CoroutineScope {
    private var _binding:FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val historyViewModel: HistoryViewModel by viewModels()

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
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val llm = LinearLayoutManager(requireContext())
        llm.stackFromEnd = true
        llm.reverseLayout = true
        llm.orientation = LinearLayoutManager.VERTICAL
        binding.rvHistory.layoutManager = llm
        binding.ivClean.setOnClickListener {
            context?.let { it1 ->
                ConfirmDialog().invoke(it1,R.string.do_you_want_to_clean_history){
                    historyViewModel.cleanHistory()
                }
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        job = SupervisorJob()
        launch {
            historyViewModel.savedMessages.collect{
                binding.ivEmpty.visibility = if(it.isNotEmpty()) View.GONE else View.VISIBLE
                binding.rvHistory.adapter =
                    parentFragment?.context?.let { it1 ->
                        HistoryAdapter(it1,it){ msgId->
                            historyViewModel.deleteFromHistory(msgId)
                        }
                    }
            }
        }

    }

    override fun onStop() {
        job.cancel()
        super.onStop()
    }

}