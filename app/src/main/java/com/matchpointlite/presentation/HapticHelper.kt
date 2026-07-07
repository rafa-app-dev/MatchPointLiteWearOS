package rc.MatchPointLite.presentation

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import rc.MatchPointLite.domain.HapticFeedbackType

object HapticHelper {

    fun trigger(context: Context, type: HapticFeedbackType) {
        val vibrator = getVibrator(context) ?: return
        val effect: VibrationEffect = when (type) {
            HapticFeedbackType.POINT -> VibrationEffect.createOneShot(
                30L, VibrationEffect.DEFAULT_AMPLITUDE
            )
            HapticFeedbackType.GAME -> VibrationEffect.createWaveform(
                longArrayOf(0, 40, 60, 40), -1
            )
            HapticFeedbackType.SET -> VibrationEffect.createWaveform(
                longArrayOf(0, 50, 50, 50, 50, 80), -1
            )
            HapticFeedbackType.UNDO -> VibrationEffect.createWaveform(
                longArrayOf(0, 20, 30, 15), -1
            )
            HapticFeedbackType.STAR_POINT -> VibrationEffect.createWaveform(
                longArrayOf(0, 60, 80, 60), -1
            )
        }
        vibrator.vibrate(effect)
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
