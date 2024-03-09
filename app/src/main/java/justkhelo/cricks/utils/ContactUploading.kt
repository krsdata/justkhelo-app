package justkhelo.cricks.utils

import android.app.Activity
import android.provider.ContactsContract
import android.util.Log
import justkhelo.cricks.models.Contact
import justkhelo.cricks.models.Phone
import kotlinx.coroutines.delay

object ContactUploading{
    var infos: ArrayList<Contact> = ArrayList()
    var canStopContact: Boolean = false
    init {
        infos.clear()
    }


     suspend fun getAllContacts(activity: Activity) {
        val contactResolver = activity.contentResolver
        val cursor = contactResolver.query(
            ContactsContract.Contacts.CONTENT_URI, arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            ), null, null, null
        )
        if (cursor!!.count > 0) while (cursor.moveToNext()) {
            if (canStopContact) {
                break
            }
            val displayName =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            // String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
            val contactId =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            //  String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Log.d("TAG", " id: $contactId")
            Log.d("TAG", " Name: $displayName")
            val ffg = Contact()
            ffg.id = (contactId)
            ffg.name = (displayName)
            val phones = ArrayList<Phone>()
            val emails = ArrayList<String>()
            val websites = ArrayList<String>()
            if (cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    .toInt() > 0
            ) {
                val pCur = contactResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(contactId),
                    null
                )
                while (pCur!!.moveToNext()) {
                    val phone =
                        pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    val type =
                        pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
                    val s = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                        activity.resources, type.toInt(), ""
                    ) as String
                    Log.d("TAG", "$s phone: $phone")
                    phones.add(Phone(s, phone))
                }
                pCur.close()
            }
            ffg.phoneList = (phones)
            val emailCursor = contactResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                arrayOf(contactId),
                null
            )
            while (emailCursor!!.moveToNext()) {
                val email =
                    emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                val type =
                    emailCursor.getInt(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))
                val s = ContactsContract.CommonDataKinds.Email.getTypeLabel(
                    activity.resources, type, ""
                ) as String
                Log.d("TAG", "$s email: $email")
                emails.add(email)
            }
            emailCursor.close()
            ffg.Emails = (emails)
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Website.URL,
                ContactsContract.CommonDataKinds.Website.TYPE
            )
            val selection =
                ContactsContract.Data._ID + " = " + contactId + " AND " + ContactsContract.Contacts.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE + "'"
            val websiteCursor = activity.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                selection,
                null,
                null
            )
            while (websiteCursor!!.moveToNext()) {
                val website = websiteCursor.getString(
                    websiteCursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.DATA)
                )
                val type =
                    websiteCursor.getInt(websiteCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))
                val s = ContactsContract.CommonDataKinds.Email.getTypeLabel(
                    activity.resources, type, ""
                ) as String
                Log.d("TAG", "$s email: $website")
                websites.add(website)
            }
            emailCursor.close()
            ffg.websites = (websites)
            infos.add(ffg)
        }
        cursor.close()


    }

}
