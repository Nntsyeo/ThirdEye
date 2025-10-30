package com.ffalcon.mercury.android.sdk.demo.ui.entity

data class Contact(
    val displayName: String,
    val phoneNum: String,
    val id: Long,

    ) {
    companion object {
        val Invalid = Contact(
            id = -1,
            displayName = "",
            phoneNum = ""
        )
    }
}

fun contactList(placeHolder:Boolean = false): ArrayList<Contact> {
    val retVal = arrayListOf<Contact>()
    for (i in 0..100L) {
        retVal.add(Contact("James - $i", "XXXXXXXX", i))
    }
    // place holder
    if (placeHolder) {
        for (i in 0..1) {
            retVal.add(Contact.Invalid)
        }
    }

    return retVal
}