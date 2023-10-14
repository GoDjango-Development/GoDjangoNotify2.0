package com.godjango.godjangonotify20.ui.adapters

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.godjango.godjangonotify20.R
import com.godjango.godjangonotify20.data.models.Message
import com.nerox.client.Tfprotocol
import com.nerox.client.callbacks.ITfprotocolCallback
import com.nerox.client.misc.StatusInfo
import com.nerox.client.misc.StatusServer
import java.io.File

class HistoryAdapter(val context: Context, val messages: List<Message>,val onConfirmDelete:(Int)->Unit) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder?>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view: View = layoutInflater.inflate(R.layout.item_ticket_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.whatsapp.setOnClickListener {
            callWhatsApp(
                holder.phone.text.toString(), context
            )
        }
        holder.w4b.setOnClickListener {
            callWhatsAppB(
                holder.phone.text.toString(), context
            )
        }
        holder.call.setOnClickListener {
            val i = Intent(Intent.ACTION_DIAL)
            i.data = Uri.parse("tel:" + holder.phone.text)
            context.startActivity(i)
        }
        holder.copy.setOnClickListener {
            val myClipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val myClip: ClipData
            var text = context.resources.getString(R.string.name) +": "+ holder.name.text.toString() + '\n'
            text+= context.resources.getString(R.string.date) +": "+ holder.fecha.text.toString() + '\n'
            text+= context.resources.getString(R.string.phone) +": "+ holder.phone.text.toString() + '\n'
            text+='\n'
            text+= holder.msg.text.toString()
            myClip = ClipData.newPlainText("text", text)
            myClipboard.setPrimaryClip(myClip)
            Toast.makeText(
                context,
                context.resources.getString(R.string.copy_text),
                Toast.LENGTH_SHORT
            ).show()
        }
        holder.delete.setOnClickListener {
            AlertDialog.Builder(context)
                .setMessage(context.resources.getString(R.string.deleteAll_pedidos))
                .setPositiveButton(
                    context.resources.getString(android.R.string.ok)
                ) { _, _ -> onConfirmDelete(messages[position].id) }.setNegativeButton(
                    context.resources.getString(android.R.string.cancel)
                ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .create().show()
        }
        holder.name.text = messages[position].name?.replace("-", "\n-\n")
        holder.phone.text = messages[position].tel
        holder.address.text = messages[position].addr
        holder.msg.text = messages[position].msg?.replace("•", "\n•")?.trim()
        holder.total_price.text = messages[position].total
        holder.fecha.text = messages[position].date?.split("\\.")?.get(0)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var call: ImageButton
        var whatsapp: ImageButton
        var w4b: ImageButton
        var copy: ImageButton
        var delete: ImageButton
        var name: TextView
        var phone: TextView
        var address: TextView
        var msg: TextView
        var total_price: TextView
        var fecha: TextView

        init {
            call = itemView.findViewById(R.id.btn_call)
            whatsapp = itemView.findViewById(R.id.btn_whatsapp)
            w4b = itemView.findViewById(R.id.btn_whatsapp_b)
            copy = itemView.findViewById(R.id.btn_copy)
            delete = itemView.findViewById(R.id.btn_delete)
            name = itemView.findViewById(R.id.tv_name)
            phone = itemView.findViewById(R.id.tv_telf)
            address = itemView.findViewById(R.id.tv_addr)
            msg = itemView.findViewById(R.id.tv_message)
            total_price = itemView.findViewById(R.id.tv_total)
            fecha = itemView.findViewById(R.id.tv_date)
        }
    }
}