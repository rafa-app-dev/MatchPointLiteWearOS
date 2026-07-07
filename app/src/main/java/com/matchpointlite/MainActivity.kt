package rc.MatchPointLite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.*
import androidx.wear.compose.material.MaterialTheme
import rc.MatchPointLite.data.BillingManager
import rc.MatchPointLite.data.MatchPointManager
import rc.MatchPointLite.presentation.MatchPointApp
import rc.MatchPointLite.presentation.PaywallScreen

class MainActivity : ComponentActivity() {

    private val vm: MatchPointManager by viewModels()
    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        billingManager = BillingManager(this)
        billingManager.startConnection()

        setContent {
            val isPurchased by billingManager.isPurchased.collectAsState()
            val state by vm.uiState.collectAsState()

            // Keep ViewModel in sync with purchase state
            LaunchedEffect(isPurchased) {
                vm.isPremium = isPurchased
            }

            MaterialTheme {
                if (state.showPaywall && !isPurchased) {
                    PaywallScreen(
                        onPurchase = {
                            billingManager.launchPurchase(this@MainActivity)
                        },
                        onDismiss = {
                            vm.dismissPaywall()
                        }
                    )
                } else {
                    MatchPointApp(vm = vm)
                }
            }
        }
    }
}
