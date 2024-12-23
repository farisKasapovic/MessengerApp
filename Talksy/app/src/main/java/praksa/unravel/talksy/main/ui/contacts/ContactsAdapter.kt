package praksa.unravel.talksy.ui.contacts

import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import praksa.unravel.talksy.R
import praksa.unravel.talksy.main.model.Contact
import praksa.unravel.talksy.main.ui.contacts.MenuType
import praksa.unravel.talksy.main.ui.contacts.formatLastSeen
import java.util.Locale


class ContactsAdapter(
    private val items: MutableList<Any>, // Kontakti i meni stavke
    private val onProfilePictureLoad: (String, ImageView) -> Unit,
    private val onMenuItemClick: (MenuType) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_CONTACT = 0
        const val TYPE_MENU_ITEM = 1
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val firstName: TextView = itemView.findViewById(R.id.firstNameTV)
        val lastName: TextView = itemView.findViewById(R.id.lastNameTV)
        val activityStatus: TextView = itemView.findViewById(R.id.activityTV)
        val picture: ImageView = itemView.findViewById(R.id.pictureIV)
    }

    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.iconImageIV)
        val text: TextView = itemView.findViewById(R.id.itemTextTV)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Contact -> TYPE_CONTACT
            else -> TYPE_MENU_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_CONTACT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.contact_item, parent, false)
                ContactViewHolder(view)
            }
            TYPE_MENU_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.menu_item, parent, false)
                MenuViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ContactViewHolder -> {
                val contact = items[position] as Contact
                holder.firstName.text = contact.firstName
                holder.lastName.text = contact.lastName
                onProfilePictureLoad(contact.id, holder.picture)
                //Aktivnost
//                holder.activityStatus.text = if (contact.isOnline) {
//                    "Online"
//                } else {
//                    "Last seen: ${contact.lastSeen?.toDate()}"
//                }
                // Display activity status
                // UVIJEK JE FALSE
                holder.activityStatus.text = if (contact.isOnline) {
                    "Online"
                } else {
                    formatLastSeen(contact.lastSeen)
//                    contact.lastSeen?.let {
//                        val date = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
//                            .format(it.toDate())
//                        "Last seen: $date"
//                    } ?: "Offline"
                }
            }
            is MenuViewHolder -> {
//                val menuPosition = position
//                when (menuPosition) {
//                    0 -> {
//                        holder.icon.setImageResource(R.drawable.location)
//                        holder.text.text = "Find people"
//                    }
//                    1 -> {
//                        holder.icon.setImageResource(R.drawable.invitefriends)
//                        holder.text.text = "Invite People"
//                    }
//                    2 -> {
//                        holder.icon.setImageResource(R.drawable.add)
//                        holder.text.text = "New Contact"
//                    }
//                }
                val menuType = when(position){
                    0 -> MenuType.FIND_PEOPLE
                    1 -> MenuType.INVITE_PEOPLE
                    2 -> MenuType.NEW_CONTACT
                    else -> throw IllegalArgumentException("Unknown menu position")
                }
                holder.icon.setImageResource(menuType.menuIconId)
                holder.text.text = holder.itemView.context.getString(menuType.menuTextId)
                holder.itemView.setOnClickListener { onMenuItemClick(menuType) }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}




/*
Prilikom dodavanja broja treba vidjeti da li ta osoba uopste postoji u bazi sa tim brojem jer inace taj korisnik ne koristi nasu aplikaciju
Treba dokument id i field id u contact biti == uid.Usera tog korisnika kojeg dodamo

 */