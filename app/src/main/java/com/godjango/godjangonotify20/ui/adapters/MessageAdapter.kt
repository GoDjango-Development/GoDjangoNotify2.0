package com.godjango.godjangonotify20.ui.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.godjango.godjangonotify20.R
import com.godjango.godjangonotify20.data.models.Message
import com.google.android.material.snackbar.Snackbar

class MessageAdapter(
    private val messages: List<Message>,
    private val context: Context,
    private val onScreen: (id:Int)->Unit,
    private val onArchive: (Message)->Unit
) : RecyclerView.Adapter<MessageAdapter.ViewHolder?>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = layoutInflater.inflate(R.layout.item_ticket_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onScreen(messages[position].id)
        holder.whatsapp.setOnClickListener {
            callWhatsApp(
                holder.phone.text.toString(),context
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
        holder.archive.setOnClickListener {
            onArchive(messages[position])
            Toast.makeText(
                context,
                context.resources.getString(R.string.saved),
                Toast.LENGTH_SHORT
            ).show()
        }
        holder.name.text = messages[position].name?.replace("-", "\n-\n")
        holder.phone.text = messages[position].tel
        holder.address.text = messages[position].addr
        holder.msg.text = messages[position].msg?.replace("•", "\n•")?.trim()
        holder.total_price.text = messages[position].total
        holder.fecha.text = messages[position].date?.split("\\.")?.get(0)
        holder.config.text = messages[position].configAndFolder?.substringBefore("::")
        holder.folder.text = messages[position].configAndFolder?.substringAfter("::")
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var call: ImageButton
        var whatsapp: ImageButton
        var w4b: ImageButton
        var copy: ImageButton
        var archive: ImageButton
        var name: TextView
        var phone: TextView
        var address: TextView
        var msg: TextView
        var total_price: TextView
        var fecha: TextView
        var config: TextView
        var folder: TextView

        init {
            call = itemView.findViewById(R.id.btn_call)
            whatsapp = itemView.findViewById(R.id.btn_whatsapp)
            w4b = itemView.findViewById(R.id.btn_whatsapp_b)
            copy = itemView.findViewById(R.id.btn_copy)
            archive = itemView.findViewById(R.id.btn_archive)
            name = itemView.findViewById(R.id.tv_name)
            phone = itemView.findViewById(R.id.tv_telf)
            address = itemView.findViewById(R.id.tv_addr)
            msg = itemView.findViewById(R.id.tv_message)
            total_price = itemView.findViewById(R.id.tv_total)
            fecha = itemView.findViewById(R.id.tv_date)
            config = itemView.findViewById(R.id.tv_config)
            folder = itemView.findViewById(R.id.tv_folder)
        }
    }
}
fun callWhatsApp(phone: String, context: Context) {
    try{
        val url = "https://api.whatsapp.com/send?phone=$phone"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        i.setPackage("com.whatsapp")
        context.startActivity(i)
    }catch (e:Exception){
        Toast.makeText(context, context.getString(R.string.install_whatsapp), Toast.LENGTH_SHORT).show()
    }
}

fun callWhatsAppB(phone: String, context: Context) {
    try{
        val url = "https://api.whatsapp.com/send?phone=$phone"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        i.setPackage("com.whatsapp.w4b")
        context.startActivity(i)
    }catch (e:Exception){
        Toast.makeText(context, context.getString(R.string.install_whatsapp_business), Toast.LENGTH_SHORT).show()
    }
}