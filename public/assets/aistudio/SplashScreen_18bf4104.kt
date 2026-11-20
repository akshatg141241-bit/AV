package com.aryaleelastudios.kundaliguru.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.kundaliguru.R
import com.aryaleelastudios.kundaliguru.ui.theme.CreamBackground
import com.aryaleelastudios.kundaliguru.ui.theme.DeepIndigo
import com.aryaleelastudios.kundaliguru.ui.theme.SaffronAccent
import com.aryaleelastudios.kundaliguru.ui.theme.SaffronLight
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isOnboarded: Boolean?,
    onNavigateNext: (destination: String) -> Unit
) {
    val scaleAnim = rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    var minSplashElapsed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1200) // 1.2 seconds splash per specification
        minSplashElapsed = true
    }

    LaunchedEffect(minSplashElapsed, isOnboarded) {
        if (minSplashElapsed && isOnboarded != null) {
            if (isOnboarded == true) {
                onNavigateNext("main")
            } else if (isOnboarded == false) {
                onNavigateNext("onboarding")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen_container")
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CreamBackground,
                        SaffronLight.copy(alpha = 0.35f),
                        CreamBackground
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Centered gold Om / Kundali wheel icon
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scaleAnim.value)
                    .clip(CircleShape)
                    .background(DeepIndigo)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_kundali_logo),
                    contentDescription = "Kundali Guru Logo",
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Kundali Guru",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DeepIndigo,
                letterSpacing = 1.sp
            )

            Text(
                text = "कुंडली गुरु",
                style = MaterialTheme.typography.titleMedium,
                color = SaffronAccent,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(36.dp))

            // Studio branding
            Text(
                text = "AryaLeela Studios",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = DeepIndigo.copy(alpha = 0.7f),
                letterSpacing = 1.5.sp,
                modifier = Modifier.testTag("splash_studio_branding")
            )
        }
    }
}
