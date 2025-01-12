package de.hbch.traewelling.api.models.trip

import com.google.gson.annotations.SerializedName
import de.hbch.traewelling.R

data class HafasLine(
    @SerializedName("type") val type: String,
    @SerializedName("id") val id: String,
    @SerializedName("fahrtNr") val journeyNumber: String?,
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
        override fun getIcon(operator: String?, line: String?) = R.drawable.ic_ferry
        override fun getString() = R.string.product_type_ferry
    },

    @SerializedName("taxi")
    TAXI {
        override fun getIcon(operator: String?, line: String?): Int = R.drawable.ic_taxi
    },

    @SerializedName("bus")
    BUS {
        override fun getIcon(operator: String?, line: String?): Int = R.drawable.ic_bus
        override fun getString() = R.string.product_type_bus
    },

    @SerializedName("suburban")
    SUBURBAN {
        override fun getIcon(operator: String?, line: String?): Int = when (operator) {
            // okay, easy one in a country with no open access operators
            "sncb" -> R.drawable.ic_suburban_be
            // obviously our alpine friends need 8 different operators and 3 different logos for S-Bahn
            "osterreichische-bundesbahnen" -> when (line) {
                // for weird reasons, one line in Vienna uses another S-Bahn logo
                "4-81-45" -> R.drawable.ic_suburban_at_stammstrecke_wien
                // salzburg S 2 and S 3 use a different logo
                "4-81-2-1451275-5318936" -> R.drawable.ic_suburban_at_salzburg
                "4-81-3-1451275-5318936" -> R.drawable.ic_suburban_at_salzburg
                else -> R.drawable.ic_suburban_at
            }
            // DB Regio operates one S-Bahn line in Salzburg but none in Germany, so easy to match
            "bayerische-regiobahn" -> R.drawable.ic_suburban_at_salzburg
            "graz-koflacher-bahn-und-busbetrieb-gmbh" -> R.drawable.ic_suburban_at
            "montafonerbahn" -> R.drawable.ic_suburban_at
            "steiermarkbahn-und-bus-gmbh" -> R.drawable.ic_suburban_at
            "stern-hafferl-verkehrs-gmbh" -> R.drawable.ic_suburban_at
            "salzburger-lokalbahnen" -> R.drawable.ic_suburban_at_salzburg
            // DB Regio operates two S-Bahn lines in Tyrol
            "db-regio-ag-bayern" -> when (line) {
                "4-800790-6" -> R.drawable.ic_suburban_at
                "4-800790-7" -> R.drawable.ic_suburban_at
                else -> R.drawable.ic_suburban_de
            }
            // Bern is the only municipality with a dedicated S-Bahn logo
            "regionalverkehr-bern-solothurn" -> R.drawable.ic_suburban_ch_bern
            // the one historic Esko line not operated by CD is not in HAFAS anyway
            "ceske-drahy" -> R.drawable.ic_suburban_cz
            // Denmark easy as always
            "dsb-s-tog" -> R.drawable.ic_suburban_dk
            // France only has one S-Bahn system properly available as such in HAFAS
            "sncf" -> R.drawable.ic_suburban_fr
            // the one country where they have a unified S-Bahn logo despite 100 different systems and operators
            else -> R.drawable.ic_suburban_de
        }

        override fun getString() = R.string.product_type_suburban
    },

    @SerializedName("subway")
    SUBWAY {
        override fun getIcon(operator: String?, line: String?): Int = R.drawable.ic_subway
        override fun getString() = R.string.product_type_subway
    },

    @SerializedName("tram")
    TRAM {
        override fun getIcon(operator: String?, line: String?): Int = R.drawable.ic_tram
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
        override fun getIcon(operator: String?, line: String?): Int = R.drawable.ic_plane
    },
    LONG_DISTANCE {
        override fun getString() = R.string.product_type_national_express
    },
    UNKNOWN {
        override fun getIcon(operator: String?, line: String?): Int = R.drawable.ic_unknown
        override fun getString() = R.string.unknown
    };

    open fun getIcon(operator: String?, line: String?) = R.drawable.ic_train
    open fun getString() = R.string.product_type_bus
}
