package com.example.mobilepay.entity

import com.example.mobilepay.ui.mainPage.Record
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import java.math.BigDecimal
import java.util.*

data class BillRecord(
    @JsonProperty("overviewInfo")val overviewInfo: OverviewInfo,
    @JsonProperty("billType")val billType:BillType,
    @JsonProperty("recordId")val recordId:Int,
    @JsonProperty("amount")val amount:BigDecimal,
    @JsonProperty("date")val date:Date,
    @JsonProperty("extraData")val extraData:Map<String, Any>
): Record

enum class BillType(@JsonValue val prompt:String) {
    transfer_in("Transfer In"),
    transfer_out("Transfer Out"),
    unrefunded_pay("Pay"),
    refunded_pay("Pay(Refunded)"),
    import_from_user("Import From Individual Account"),
    import_from_merchant("Import From Merchant Account"),
    export_to_user("Export To Individual Account"),
    export_to_merchant("Export To Merchant Account"),
}
