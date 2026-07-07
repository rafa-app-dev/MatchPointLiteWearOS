package rc.MatchPointLite.domain

enum class PointValue(val display: String) {
    ZERO("0"),
    FIFTEEN("15"),
    THIRTY("30"),
    FORTY("40"),
    AD("AD");

    fun next(): PointValue = when (this) {
        ZERO     -> FIFTEEN
        FIFTEEN  -> THIRTY
        THIRTY   -> FORTY
        FORTY    -> FORTY
        AD       -> AD
    }
}

enum class HapticFeedbackType { POINT, GAME, SET, UNDO, STAR_POINT }

data class MatchState(
    val userPoints: PointValue,
    val opponentPoints: PointValue,
    val userGames: Int,
    val opponentGames: Int,
    val userSets: Int,
    val opponentSets: Int,
    val isTieBreak: Boolean,
    val userTBPoints: Int,
    val opponentTBPoints: Int,
    val servingUser: Boolean,
    val isGoldenPoint: Boolean,
    val isStarPointEnabled: Boolean,
    val deuceCount: Int
)
