package com.example.mobilepay.entity

import android.os.Parcelable
import com.example.mobilepay.ui.mainPage.Record
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.math.BigDecimal
import java.util.*

@Parcelize
data class BillRecord(
    @JsonProperty("overviewInfo")val overviewInfo: OverviewInfo,
    @JsonProperty("billType")val billType:BillType,
    @JsonProperty("recordId")val recordId:Int,
    @JsonProperty("amount")val amount:BigDecimal,
    @JsonProperty("date")val date:Date,
    @JsonProperty("extraData")  val extraData:@RawValue Map<String, Any>
): Record, Parcelable {




    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BillRecord

        if (overviewInfo != other.overviewInfo) return false
        if (billType != other.billType) return false
        if (recordId != other.recordId) return false
        if (amount != other.amount) return false
        if (date != other.date) return false
        if (extraData != other.extraData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = overviewInfo.hashCode()
        result = 31 * result + billType.hashCode()
        result = 31 * result + recordId
        result = 31 * result + amount.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + extraData.hashCode()
        return result
    }
}

enum class BillType(@JsonValue val prompt:String) {
    transfer_in("Transfer In"),
    transfer_out("Transfer Out"),
    pay("Pay"),
    refunded_pay("Pay(Refunded)"),
    import_from_user("Import From Individual Account"),
    import_from_merchant("Import From Merchant Account"),
    export_to_user("Export To Individual Account"),
    export_to_merchant("Export To Merchant Account"),
}
