package it.sonix.connect

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.sonix.connect.screens.AboutScreen
import it.sonix.connect.screens.HomeScreen
import it.sonix.connect.screens.SettingsScreen

@Composable

fun DesktopApp() {

    var screen by remember { mutableStateOf(DesktopScreen.HOME) }

    Row(Modifier.fillMaxSize()) {
        DesktopSidebar(
            selected = screen,
            onSelect = { screen = it }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF26272e))
        ) {

            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith
                            fadeOut(animationSpec = tween(220))
                },
                label = "page-transition"
            ) { targetScreen ->

                when (targetScreen) {
                    DesktopScreen.HOME -> HomeScreen()
                    DesktopScreen.SETTINGS -> SettingsScreen()
                    DesktopScreen.ABOUT -> AboutScreen()
                }
            }
        }
    }
}


enum class DesktopScreen { HOME, SETTINGS, ABOUT }
@Composable
fun DesktopSidebar(
    selected: DesktopScreen,
    onSelect: (DesktopScreen) -> Unit
) {
    Column(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .padding(vertical = 20.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        DesktopNavItem(
            label = "Home",
            selected = selected == DesktopScreen.HOME,
            onClick = { onSelect(DesktopScreen.HOME) }
        )

        DesktopNavItem(
            label = "Settings",
            selected = selected == DesktopScreen.SETTINGS,
            onClick = { onSelect(DesktopScreen.SETTINGS) }
        )

        DesktopNavItem(
            label = "About",
            selected = selected == DesktopScreen.ABOUT,
            onClick = { onSelect(DesktopScreen.ABOUT) }
        )
    }
}

@Composable
fun DesktopNavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val targetBackground = when {
        selected ->
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        isHovered ->
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
        else ->
            Color.Transparent
    }

    val animatedBackground by animateColorAsState(
        targetValue = targetBackground,
        animationSpec = tween(
            durationMillis = 140,
            easing = FastOutSlowInEasing
        ),
        label = "nav-bg"
    )

    val animatedIndicatorHeight by animateDpAsState(
        targetValue = if (selected) 18.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 160,
            easing = FastOutSlowInEasing
        ),
        label = "nav-indicator"
    )

    val textColor by animateColorAsState(
        targetValue =
            if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(120),
        label = "nav-text"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(animatedBackground, RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null, // ❌ no ripple (desktop)
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Animated selection indicator
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(animatedIndicatorHeight)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(2.dp)
                )
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight =
                if (selected)
                    FontWeight.SemiBold
                else
                    FontWeight.Normal
        )
    }
}
