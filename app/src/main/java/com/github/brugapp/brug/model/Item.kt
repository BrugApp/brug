package com.github.brugapp.brug.model

import com.github.brugapp.brug.R
import java.lang.IllegalArgumentException

class Item// Generate the id later
    (private var name: String, private var description : String, private var id : Int) {

    // add QR code attribute
    // add last localization attribute
    // image_id attribute is for future use (with image type)

    private var lost: Boolean = false
    private var type:ItemType = ItemType.Other
    private var iconId:Int = 0

    init {
        if(name.isBlank()){
            throw IllegalArgumentException("Invalid name")
        }
    }

    fun isLost():Boolean{
        return lost
    }

    fun setLost(value:Boolean):Item{
        this.lost = value
        return this
    }

    fun getName() : String{
        return name
    }

    fun getId() : Int{
        return id
    }

    fun getDescription() : String{
        return description
    }

    fun setName(name : String):Item{

        if(name.isBlank()){
            throw IllegalArgumentException("Invalid new name")
        }

        this.name = name
        return this
    }

    fun setType(type: ItemType):Item{
        this.type = type
        iconId = when (type) {
            ItemType.Wallet -> R.drawable.ic_baseline_account_balance_wallet_24
            ItemType.Keys -> R.drawable.ic_baseline_vpn_key_24
            ItemType.CarKeys -> R.drawable.ic_baseline_car_rental_24
            ItemType.Phone -> R.drawable.ic_baseline_smartphone_24
            ItemType.Other -> R.drawable.ic_baseline_add_24
        }
        return this
    }

    fun getIcon():Int{
        return iconId
    }

    fun setDescription(description : String){
        this.description = description
    }
}
enum class ItemType { Wallet, Keys,CarKeys,Phone,Other }
