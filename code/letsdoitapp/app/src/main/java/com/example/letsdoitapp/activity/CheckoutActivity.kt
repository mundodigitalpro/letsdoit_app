package com.example.letsdoitapp.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.letsdoitapp.R
import com.example.letsdoitapp.activity.MainActivity.Companion.isPremium
import com.example.letsdoitapp.utils.Utility.showCustomSnackbar
import com.paypal.checkout.approve.OnApprove
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.createorder.CreateOrder
import com.paypal.checkout.createorder.CurrencyCode
import com.paypal.checkout.createorder.OrderIntent
import com.paypal.checkout.createorder.UserAction
import com.paypal.checkout.error.OnError
import com.paypal.checkout.order.Amount
import com.paypal.checkout.order.AppContext
import com.paypal.checkout.order.Order
import com.paypal.checkout.order.PurchaseUnit
import com.paypal.checkout.paymentbutton.PaymentButtonContainer

class CheckoutActivity : AppCompatActivity() {
    private lateinit var paymentButtonContainer: PaymentButtonContainer
    private lateinit var lyCheckout: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        lyCheckout = findViewById(R.id.activity_checkout)
        paymentButtonContainer = findViewById(R.id.payment_button_container)
        paymentButtonContainer.setup(
            createOrder =
            CreateOrder { createOrderActions ->
                val order =
                    Order(
                        intent = OrderIntent.CAPTURE,
                        appContext = AppContext(userAction = UserAction.PAY_NOW),
                        purchaseUnitList =
                        listOf(
                            PurchaseUnit(
                                amount =
                                Amount(currencyCode = CurrencyCode.USD, value = "5.00")
                            )
                        )
                    )
                createOrderActions.create(order)
            },
            onApprove =
            OnApprove { approval ->
                approval.orderActions.capture { captureOrderResult ->
                    //here you will get the result (Success)
                    Log.i("CaptureOrder", "CaptureOrderResult: $captureOrderResult")
                    //showCustomSnackbar(lyCheckout, "Resultado del Pedido: $captureOrderResult", R.color.orange_strong, 10000)
                    isPremium = true
                    MainActivity.becamePremium()
                     showCustomSnackbar(lyCheckout, "El pago por PayPal se ha realizado con exito",
                         R.color.orange_strong, 3000)


                    Handler(Looper.getMainLooper()).postDelayed({
                        // Código para mostrar el mensaje después de 5 segundos
                        goHome()
                    }, 3000) // 5000 milisegundos = 5 segundos

                }
            },
            onCancel = OnCancel {
                Log.d("OnCancel", "Buyer canceled the PayPal experience.")
                showCustomSnackbar(lyCheckout, "Se ha cancelado el pago por PayPal",
                    R.color.orange_strong, 3000)
                //checkPremium = false
            },
            onError = OnError { errorInfo ->
                Log.d("OnError", "Error: $errorInfo")
                showCustomSnackbar(lyCheckout, "Error: $errorInfo", R.color.orange_strong, 3000)
            }
        )//Setup
    }

    private fun goHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
