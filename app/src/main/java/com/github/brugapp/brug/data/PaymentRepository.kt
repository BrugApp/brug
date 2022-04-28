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
                    .put("parameters",JSONObject().put("gateway","example").put("gatewayMerchantId","exampleGatewayMerchantId"))))
            )
            .put("transactionInfo",JSONObject()
                .put("totalPriceStatus","FINAL")
                .put("totalPrice","12.34")
                .put("currencyCode","USD"))
        return IsReadyToPayRequest.fromJson(jsonDerulo.toString())
    }

    private fun updateOnSuccess(){
        val paymentRequest = paymentRequest()
        paymentsClient.isReadyToPay(paymentRequest)

            .addOnCompleteListener {
                if(it.result) {
                    //showGooglePayButton(completeTask.getResult())
                }else {
                    //handle the payment error
                    //Toast.makeText(this, "Credit card payment failed", Toast.LENGTH_LONG).show()
                }
            }
    }
}