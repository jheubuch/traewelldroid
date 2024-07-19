package de.hbch.traewelling.api.models.trip

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.R

data class HafasLine(
    @SerializedName("type") val type: String,
    @SerializedName("id") val id: String,
    @SerializedName("fahrtNr") val journeyNumber: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("product") val product: ProductType?,
    @SerializedName("operator") val operator: HafasOperator?,
    val productName: String
) {
    val safeProductType get() = product ?: ProductType.UNKNOWN
}

@Suppress("unused")
enum class ProductType {
    @SerializedName("all")
    ALL,
    @SerializedName("ferry")
    FERRY {
        override fun getIcon() = R.drawable.ic_ferry
        override fun getString() = R.string.product_type_ferry
    },
    @SerializedName("taxi")
    TAXI {
        override fun getIcon() = R.drawable.ic_taxi
    },
    @SerializedName("bus")
    BUS {
        override fun getIcon() = R.drawable.ic_bus
        override fun getString() = R.string.product_type_bus
    },
    @SerializedName("suburban")
    SUBURBAN {
        override fun getIcon() = R.drawable.ic_suburban
        override fun getString() = R.string.product_type_suburban
    },
    @SerializedName("subway")
    SUBWAY {
        override fun getIcon() = R.drawable.ic_subway
        override fun getString() = R.string.product_type_subway
    },
    @SerializedName("tram")
    TRAM {
        override fun getIcon() = R.drawable.ic_tram
        override fun getString() = R.string.product_type_tram
    },
    // RE, RB, RS
    @SerializedName("regional")
    REGIONAL {
        override fun getString() = R.string.product_type_regional
    },
    // IRE, IR
    @SerializedName("regionalExp")
    REGIONAL_EXPRESS {
        override fun getString() = R.string.product_type_regional_express
    },
    // ICE, ECE
    @SerializedName("nationalExpress")
    NATIONAL_EXPRESS {
        override fun getString() = R.string.product_type_national_express
    },
    // IC, EC
    @SerializedName("national")
    NATIONAL {
        override fun getString() = R.string.product_type_national
    },
    @SerializedName("plane")
    PLANE {
        override fun getIcon() = R.drawable.ic_plane
    },
    LONG_DISTANCE {
        override fun getString() = R.string.product_type_national_express
    },
    UNKNOWN {
        override fun getIcon() = R.drawable.ic_unknown
        override fun getString() = R.string.unknown
    };

    open fun getIcon() = R.drawable.ic_train
    open fun getString() = R.string.product_type_bus
}
