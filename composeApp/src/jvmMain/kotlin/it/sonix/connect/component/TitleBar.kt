package it.sonix.connect.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.sp
import java.awt.Frame

@Composable
fun TitleBar(window: ComposeWindow) {


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(Color(0xFF1D1F2A))
            .padding(start = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {


        Image(
            painter = painterResource("sonix_logo.png"),
            contentDescription = "Sonix Logo",
            modifier = Modifier.size(18.dp)
        )

        Spacer(Modifier.width(8.dp))

        Text(
            "Sonix Connect",
            color = Color.White,
            fontSize = 13.sp
        )

        Spacer(Modifier.weight(1f))

        TitleIconButton("minimize.png") {
            window.extendedState = Frame.ICONIFIED
        }

        TitleIconButton("maximize.png") {
            window.extendedState =
                if (window.extendedState == Frame.MAXIMIZED_BOTH)
                    Frame.NORMAL
                else
                    Frame.MAXIMIZED_BOTH
        }

        TitleIconButton(
            icon = "close.png",
            close = true
        ) {
            window.dispose()
        }
    }
}

@Composable
fun TitleIconButton(
    icon: String,
    close: Boolean = false,
    onClick: () -> Unit
) {

    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    val bg =
        when {
            close && hovered -> Color(0xFFE81123)
            hovered -> Color(0xFF2A2D3E)
            else -> Color.Transparent
        }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(46.dp)
            .background(bg)
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {

        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}