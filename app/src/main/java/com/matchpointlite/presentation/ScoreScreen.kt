package rc.MatchPointLite.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import rc.MatchPointLite.data.MatchPointManager
import rc.MatchPointLite.data.MatchUiState
import rc.MatchPointLite.domain.PointValue

private val ColorOpponent = Color(0xFFCC2A2A)
private val ColorUser     = Color(0xFF1D8A3A)
private val ColorBadge    = Color(0xFFFFD700)
private val ColorOverlay  = Color(0xF0101010)

@Composable
fun TennisBall(size: Float = 28f) {
    Canvas(modifier = Modifier.size(size.dp)) {
        val r = this.size.minDimension / 2f
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f

        // Yellow-green ball body
        drawCircle(color = Color(0xFFCCEA00), radius = r)

        // White seam curves
        val strokeWidth = r * 0.18f
        drawArc(
            color = Color.White,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            style = Stroke(width = strokeWidth),
            topLeft = androidx.compose.ui.geometry.Offset(cx - r * 0.6f, cy - r * 0.95f),
            size = androidx.compose.ui.geometry.Size(r * 1.2f, r * 1.2f)
        )
        drawArc(
            color = Color.White,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            style = Stroke(width = strokeWidth),
            topLeft = androidx.compose.ui.geometry.Offset(cx - r * 0.6f, cy - r * 0.25f),
            size = androidx.compose.ui.geometry.Size(r * 1.2f, r * 1.2f)
        )
    }
}

@Composable
fun MatchPointApp(vm: MatchPointManager = viewModel()) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    var showEditMenu by remember { mutableStateOf(false) }

    LaunchedEffect(state.lastHaptic) {
        HapticHelper.trigger(context, state.lastHaptic)
    }

    if (showEditMenu) {
        EditMenuScreen(state = state, vm = vm, onClose = { showEditMenu = false })
    } else {
        ScoreScreen(
            state = state,
            onUserScore = { vm.scorePoint(forUser = true) },
            onOpponentScore = { vm.scorePoint(forUser = false) },
            onUndo = { vm.undo() },
            onLongPress = { showEditMenu = true }
        )
    }
}

@Composable
fun ScoreScreen(
    state: MatchUiState,
    onUserScore: () -> Unit,
    onOpponentScore: () -> Unit,
    onUndo: () -> Unit,
    onLongPress: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount.x < -30f) onUndo()
                }
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            ScorePanel(
                label = "OPPONENT",
                score = if (state.isTieBreak) "${state.opponentTBPoints}"
                        else state.opponentPoints.display,
                games = state.opponentGames,
                sets  = state.opponentSets,
                color = ColorOpponent,
                isServing = !state.servingUser,
                isTop = true,
                modifier = Modifier.weight(1f),
                onClick = onOpponentScore,
                onLongPress = onLongPress
            )
            ScorePanel(
                label = "YOU",
                score = if (state.isTieBreak) "${state.userTBPoints}"
                        else state.userPoints.display,
                games = state.userGames,
                sets  = state.userSets,
                color = ColorUser,
                isServing = state.servingUser,
                isTop = false,
                modifier = Modifier.weight(1f),
                onClick = onUserScore,
                onLongPress = onLongPress
            )
        }
        CenterBadges(state = state, modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun ScorePanel(
    label: String,
    score: String,
    games: Int,
    sets: Int,
    color: Color,
    isServing: Boolean,
    isTop: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.88f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = if (isTop) 6.dp else 2.dp)
            ) {
                Text("G: $games", color = Color.White, fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                Spacer(Modifier.width(12.dp))
                Text("S: $sets", color = Color.White, fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace, fontSize = 9.sp)
            }
            Spacer(Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isServing) {
                    TennisBall(size = 14f)
                    Spacer(Modifier.width(4.dp))
                }
                Text(text = score, color = Color.White, fontWeight = FontWeight.Black,
                    fontSize = 38.sp, textAlign = TextAlign.Center)
                if (isServing) Spacer(Modifier.width(16.dp))
            }
            Text(text = label, color = Color.White.copy(alpha = 0.7f),
                fontSize = 7.sp, letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 4.dp))
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun CenterBadges(state: MatchUiState, modifier: Modifier = Modifier) {
    if (state.isTieBreak) return
    val showDeuce  = state.deuceCount > 0
    val showStar   = state.isStarPointEnabled && state.deuceCount >= 3
    val showGolden = state.isGoldenPoint &&
                     state.userPoints == PointValue.FORTY &&
                     state.opponentPoints == PointValue.FORTY
    if (!showDeuce && !showStar && !showGolden) return

    Row(modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        if (showDeuce) {
            Badge(text = "D${state.deuceCount}",
                bg = if (showStar) ColorBadge else Color.Black,
                fg = if (showStar) Color.Black else Color.White)
        }
        if (showStar) {
            Row(modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(ColorBadge)
                .padding(horizontal = 4.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Star, contentDescription = "Star Point",
                    tint = Color.Black, modifier = Modifier.size(8.dp))
                Spacer(Modifier.width(2.dp))
                Text("STAR", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.Black)
            }
        }
        if (showGolden) {
            Badge(text = "GOLDEN PT", bg = ColorBadge, fg = Color.Black)
        }
    }
}

@Composable
private fun Badge(text: String, bg: Color, fg: Color) {
    Text(text = text, color = fg, fontSize = 7.sp, fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(bg)
            .padding(horizontal = 4.dp, vertical = 1.dp))
}

@Composable
fun EditMenuScreen(state: MatchUiState, vm: MatchPointManager, onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(ColorOverlay),
        contentAlignment = Alignment.Center) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Text("MATCH SETTINGS", color = Color.White,
                    fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
            }
            item { ToggleRow("Golden Point", state.isGoldenPoint) { vm.toggleGoldenPoint() } }
            item { ToggleRow("Star Point (3rd Deuce)", state.isStarPointEnabled) { vm.toggleStarPoint() } }
            item {
                AdjusterRow("YOUR GAMES",
                    onMinus = { vm.modifyGames(forUser = true, increment = false) },
                    onPlus  = { vm.modifyGames(forUser = true, increment = true) })
            }
            item {
                AdjusterRow("YOUR SETS",
                    onMinus = { vm.modifySets(forUser = true, increment = false) },
                    onPlus  = { vm.modifySets(forUser = true, increment = true) })
            }
            item {
                CompactChip(onClick = { vm.toggleServe() },
                    label = { Text("SWITCH SERVE", fontSize = 9.sp) },
                    colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF333333)))
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactChip(onClick = { vm.undo(); onClose() },
                        label = { Text("UNDO", fontSize = 9.sp) },
                        colors = ChipDefaults.chipColors(backgroundColor = Color(0xFFB06000)))
                    CompactChip(onClick = { vm.resetMatch(); onClose() },
                        label = { Text("RESET", fontSize = 9.sp) },
                        colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF8B0000)))
                }
            }
            item {
                CompactChip(onClick = onClose,
                    label = { Text("CLOSE", fontSize = 9.sp) },
                    colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF222222)))
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White, fontSize = 9.sp)
        Switch(checked = checked, onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = ColorBadge,
                checkedTrackColor = ColorBadge.copy(alpha = 0.4f)))
    }
}

@Composable
private fun AdjusterRow(label: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White, fontSize = 8.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CompactChip(onClick = onMinus,
                label = { Text("−", fontSize = 12.sp) },
                modifier = Modifier.size(28.dp),
                colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF333333)))
            CompactChip(onClick = onPlus,
                label = { Text("+", fontSize = 12.sp) },
                modifier = Modifier.size(28.dp),
                colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF333333)))
        }
    }
}
