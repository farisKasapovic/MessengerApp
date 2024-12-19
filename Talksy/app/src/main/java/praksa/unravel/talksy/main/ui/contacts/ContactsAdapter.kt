package praksa.unravel.talksy.ui.contacts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import praksa.unravel.talksy.R
import praksa.unravel.talksy.main.model.Contact

class ContactsAdapter(
    private val contacts: List<Contact>,
    private val onProfilePictureLoad: (String, ImageView) -> Unit  ):
    RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val firstName: TextView = itemView.findViewById(R.id.firstNameTV)
        val lastName: TextView = itemView.findViewById(R.id.lastNameTV)
        val phone: TextView = itemView.findViewById(R.id.activityTV)
        val picture: ImageView = itemView.findViewById(R.id.pictureIV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.firstName.text = contact.firstName
        holder.lastName.text = contact.lastName
        holder.phone.text = contact.phoneNumber

        onProfilePictureLoad(contact.id, holder.picture)

    }

    override fun getItemCount(): Int = contacts.size
}


/*
Prilikom dodavanja broja treba vidjeti da li ta osoba uopste postoji u bazi sa tim brojem jer inace taj korisnik ne koristi nasu aplikaciju
Treba dokument id i field id u contact biti == uid.Usera tog korisnika kojeg dodamo

 */