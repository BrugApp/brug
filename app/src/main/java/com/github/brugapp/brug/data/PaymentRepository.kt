package com.github.brugapp.brug.data

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONArray
import org.json.JSONObject

class PaymentRepository : AppCompatActivity() {

    private lateinit var paymentsClient: PaymentsClient
    private val gatewayMerchantId = "gatewayMerchantId"
    private val gateway = "gatewayExample"
    private val currency = "USD"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val walletOptions = Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build()
        paymentsClient = Wallet.getPaymentsClient(this,walletOptions)
    }

    fun paymentRequest(): IsReadyToPayRequest{ //this method should be private but we need to test it
        //version & parameter choices described here: https://developers.google.com/pay/api/android/reference/request-objects

        val jsonDerulo = JSONObject()
            .put("apiVersion",2)
            .put("apiVersionMinor",0)
            .put("merchantInfo",JSONObject().put("merchantName","Unlost"))
            .put("allowedPaymentMethods",JSONArray().put(JSONObject()
                .put("type","CARD")
                .put("parameters",JSONObject()
                    .put("allowedAuthMethods",JSONArray().put("PAN_ONLY").put("CRYPTOGRAM_3DS"))
                    .put("allowedCardNetworks",JSONArray().put("AMEX").put("DISCOVER").put("INTERAC").put("JCB").put("MASTERCARD").put("MIR").put("VISA")))
                .put("tokenizationSpecification",JSONObject()
                    .put("type","PAYMENT_GATEWAY")
                    .put("parameters",JSONObject().put("gateway",gateway).put("gatewayMerchantId",gatewayMerchantId))))
            )
            .put("transactionInfo",JSONObject()
                .put("totalPriceStatus","FINAL")
                .put("totalPrice","12.34")
                .put("currencyCode",currency))
        return IsReadyToPayRequest.fromJson(jsonDerulo.toString())
    }

    fun updateOnSuccess(){ //this method should be private but we need to test it
        val paymentRequest = paymentRequest()
        paymentsClient.isReadyToPay(paymentRequest)

            .addOnCompleteListener {
                if(it.result) {
                    //@TODO implement UI google-pay class to enable UI-updates
                    // update UI to show Google Pay Button using task result
                }else {
                    //update UI to handle the payment error with Toast.makeText(this, "Credit card payment failed", Toast.LENGTH_LONG).show()
                }
            }
    }
}