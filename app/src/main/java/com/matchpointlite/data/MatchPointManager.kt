package rc.MatchPointLite.data

import androidx.lifecycle.ViewModel
import rc.MatchPointLite.domain.HapticFeedbackType
import rc.MatchPointLite.domain.MatchState
import rc.MatchPointLite.domain.PointValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MatchUiState(
    val userPoints: PointValue = PointValue.ZERO,
    val opponentPoints: PointValue = PointValue.ZERO,
    val userGames: Int = 0,
    val opponentGames: Int = 0,
    val userSets: Int = 0,
    val opponentSets: Int = 0,
    val isTieBreak: Boolean = false,
    val userTBPoints: Int = 0,
    val opponentTBPoints: Int = 0,
    val servingUser: Boolean = true,
    val isGoldenPoint: Boolean = false,
    val isStarPointEnabled: Boolean = false,
    val deuceCount: Int = 0,
    val lastHaptic: HapticFeedbackType = HapticFeedbackType.POINT,
    val gamesPlayedTotal: Int = 0,
    val undosUsed: Int = 0,
    val showPaywall: Boolean = false
)

class MatchPointManager : ViewModel() {

    companion object {
        const val FREE_GAME_LIMIT = 3
        const val FREE_UNDO_LIMIT = 3
    }

    private val _uiState = MutableStateFlow(MatchUiState())
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()
    private val history = ArrayDeque<MatchState>()

    var isPremium: Boolean = false

    fun dismissPaywall() {
        _uiState.update { it.copy(showPaywall = false) }
    }

    fun scorePoint(forUser: Boolean) {
        saveToHistory()
        _uiState.update { it.copy(lastHaptic = HapticFeedbackType.POINT) }
        if (_uiState.value.isTieBreak) applyTieBreakLogic(forUser)
        else applyStandardLogic(forUser)
    }

    fun toggleGoldenPoint() { _uiState.update { it.copy(isGoldenPoint = !it.isGoldenPoint) } }
    fun toggleStarPoint() { _uiState.update { it.copy(isStarPointEnabled = !it.isStarPointEnabled) } }

    fun modifyGames(forUser: Boolean, increment: Boolean) {
        saveToHistory()
        _uiState.update { s ->
            if (forUser) s.copy(userGames = maxOf(0, s.userGames + if (increment) 1 else -1))
            else s.copy(opponentGames = maxOf(0, s.opponentGames + if (increment) 1 else -1))
        }
        checkSetStatus()
    }

    fun modifySets(forUser: Boolean, increment: Boolean) {
        saveToHistory()
        _uiState.update { s ->
            if (forUser) s.copy(userSets = maxOf(0, s.userSets + if (increment) 1 else -1))
            else s.copy(opponentSets = maxOf(0, s.opponentSets + if (increment) 1 else -1))
        }
    }

    fun toggleServe() { saveToHistory(); _uiState.update { it.copy(servingUser = !it.servingUser) } }

    fun resetMatch() {
        saveToHistory()
        _uiState.update { s ->
            s.copy(
                userPoints = PointValue.ZERO, opponentPoints = PointValue.ZERO,
                userGames = 0, opponentGames = 0, userSets = 0, opponentSets = 0,
                isTieBreak = false, userTBPoints = 0, opponentTBPoints = 0,
                servingUser = true, deuceCount = 0
            )
        }
    }

    fun undo() {
        if (!isPremium && _uiState.value.undosUsed >= FREE_UNDO_LIMIT) {
            _uiState.update { it.copy(showPaywall = true) }
            return
        }
        val prev = history.removeLastOrNull() ?: return
        _uiState.update {
            it.copy(
                userPoints = prev.userPoints, opponentPoints = prev.opponentPoints,
                userGames = prev.userGames, opponentGames = prev.opponentGames,
                userSets = prev.userSets, opponentSets = prev.opponentSets,
                isTieBreak = prev.isTieBreak, userTBPoints = prev.userTBPoints,
                opponentTBPoints = prev.opponentTBPoints, servingUser = prev.servingUser,
                isGoldenPoint = prev.isGoldenPoint, isStarPointEnabled = prev.isStarPointEnabled,
                deuceCount = prev.deuceCount, lastHaptic = HapticFeedbackType.UNDO,
                undosUsed = it.undosUsed + 1
            )
        }
    }

    private fun applyStandardLogic(forUser: Boolean) {
        val s = _uiState.value
        val winnerPts = if (forUser) s.userPoints else s.opponentPoints
        val loserPts = if (forUser) s.opponentPoints else s.userPoints

        if (winnerPts == PointValue.THIRTY && loserPts == PointValue.FORTY) {
            val newDeuceCount = s.deuceCount + 1
            val haptic = if (s.isStarPointEnabled && newDeuceCount == 3)
                HapticFeedbackType.STAR_POINT else HapticFeedbackType.POINT
            _uiState.update { it.copy(deuceCount = newDeuceCount, lastHaptic = haptic) }
        }

        val updatedS = _uiState.value
        val wp = if (forUser) updatedS.userPoints else updatedS.opponentPoints
        val lp = if (forUser) updatedS.opponentPoints else updatedS.userPoints

        when {
            wp == PointValue.AD -> winGame(forUser)
            wp == PointValue.FORTY -> {
                when {
                    updatedS.isStarPointEnabled && updatedS.deuceCount >= 3 -> winGame(forUser)
                    updatedS.isGoldenPoint && lp == PointValue.FORTY -> winGame(forUser)
                    lp == PointValue.FORTY -> {
                        if (forUser) _uiState.update { it.copy(userPoints = PointValue.AD) }
                        else _uiState.update { it.copy(opponentPoints = PointValue.AD) }
                    }
                    lp == PointValue.AD -> {
                        val newDeuce = updatedS.deuceCount + 1
                        val haptic = if (updatedS.isStarPointEnabled && newDeuce == 3)
                            HapticFeedbackType.STAR_POINT else HapticFeedbackType.POINT
                        if (forUser) _uiState.update { it.copy(opponentPoints = PointValue.FORTY, deuceCount = newDeuce, lastHaptic = haptic) }
                        else _uiState.update { it.copy(userPoints = PointValue.FORTY, deuceCount = newDeuce, lastHaptic = haptic) }
                    }
                    else -> winGame(forUser)
                }
            }
            else -> {
                val nextPt = wp.next()
                if (forUser) _uiState.update { it.copy(userPoints = nextPt) }
                else _uiState.update { it.copy(opponentPoints = nextPt) }
            }
        }
    }

    private fun winGame(userWon: Boolean) {
        val newTotal = _uiState.value.gamesPlayedTotal + 1
        _uiState.update { s ->
            s.copy(
                lastHaptic = HapticFeedbackType.GAME,
                userGames = if (userWon) s.userGames + 1 else s.userGames,
                opponentGames = if (!userWon) s.opponentGames + 1 else s.opponentGames,
                servingUser = !s.servingUser, deuceCount = 0,
                userPoints = PointValue.ZERO, opponentPoints = PointValue.ZERO,
                gamesPlayedTotal = newTotal
            )
        }
        if (!isPremium && newTotal >= FREE_GAME_LIMIT) {
            _uiState.update { it.copy(showPaywall = true) }
            return
        }
        checkSetStatus()
    }

    private fun checkSetStatus() {
        val s = _uiState.value
        when {
            s.userGames == 6 && s.opponentGames == 6 -> _uiState.update { it.copy(isTieBreak = true) }
            (s.userGames >= 6 && s.userGames >= s.opponentGames + 2) || s.userGames == 7 -> winSet(true)
            (s.opponentGames >= 6 && s.opponentGames >= s.userGames + 2) || s.opponentGames == 7 -> winSet(false)
        }
    }

    private fun winSet(userWon: Boolean) {
        _uiState.update { s ->
            s.copy(
                lastHaptic = HapticFeedbackType.SET,
                userSets = if (userWon) s.userSets + 1 else s.userSets,
                opponentSets = if (!userWon) s.opponentSets + 1 else s.opponentSets,
                userGames = 0, opponentGames = 0,
                userPoints = PointValue.ZERO, opponentPoints = PointValue.ZERO,
                isTieBreak = false, userTBPoints = 0, opponentTBPoints = 0, deuceCount = 0
            )
        }
    }

    private fun applyTieBreakLogic(forUser: Boolean) {
        _uiState.update { s ->
            val newUserTB = if (forUser) s.userTBPoints + 1 else s.userTBPoints
            val newOppTB = if (!forUser) s.opponentTBPoints + 1 else s.opponentTBPoints
            val total = newUserTB + newOppTB
            val switchServe = total == 1 || (total > 1 && (total - 1) % 2 == 0)
            s.copy(
                userTBPoints = newUserTB, opponentTBPoints = newOppTB,
                servingUser = if (switchServe) !s.servingUser else s.servingUser
            )
        }
        val s = _uiState.value
        when {
            s.userTBPoints >= 7 && s.userTBPoints >= s.opponentTBPoints + 2 -> {
                _uiState.update { it.copy(userGames = 7) }; winSet(true)
            }
            s.opponentTBPoints >= 7 && s.opponentTBPoints >= s.userTBPoints + 2 -> {
                _uiState.update { it.copy(opponentGames = 7) }; winSet(false)
            }
        }
    }

    private fun saveToHistory() {
        val s = _uiState.value
        history.addLast(MatchState(
            userPoints = s.userPoints, opponentPoints = s.opponentPoints,
            userGames = s.userGames, opponentGames = s.opponentGames,
            userSets = s.userSets, opponentSets = s.opponentSets,
            isTieBreak = s.isTieBreak, userTBPoints = s.userTBPoints,
            opponentTBPoints = s.opponentTBPoints, servingUser = s.servingUser,
            isGoldenPoint = s.isGoldenPoint, isStarPointEnabled = s.isStarPointEnabled,
            deuceCount = s.deuceCount
        ))
    }
}
