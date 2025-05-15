package com.example.fedger.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.IntOffset

// Animation durations for consistent experience across the app
const val ANIMATION_DURATION_SHORTEST = 100
const val ANIMATION_DURATION_SHORT = 200
const val ANIMATION_DURATION_MEDIUM = 300
const val ANIMATION_DURATION_LONG = 450

// Enhanced spring animation specs for bouncy, playful animations
val SpringSpecBouncy = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium,
    visibilityThreshold = IntOffset.VisibilityThreshold
)

val SpringSpecLowBouncy = spring(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow,
    visibilityThreshold = IntOffset.VisibilityThreshold
)

// Standard tween specs for smooth transitions
val TweenSpecFast = tween<Float>(
    durationMillis = ANIMATION_DURATION_SHORT,
    easing = FastOutSlowInEasing
)

val TweenSpecMedium = tween<Float>(
    durationMillis = ANIMATION_DURATION_MEDIUM,
    easing = EaseInOutCubic
)

// Screen transitions for better navigation experience
object ScreenTransitions {
    // Enter transitions
    val slideInFromRight: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseInOutCubic)
    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION_MEDIUM))
    
    val slideInFromLeft: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseInOutCubic)
    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION_MEDIUM))
    
    val slideInFromBottom: EnterTransition = slideInVertically(
        initialOffsetY = { fullHeight -> fullHeight },
        animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseInOutCubic)
    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION_MEDIUM))
    
    val fadeIn: EnterTransition = androidx.compose.animation.fadeIn(
        animationSpec = tween(ANIMATION_DURATION_MEDIUM)
    )
    
    val scaleIn: EnterTransition = scaleIn(
        initialScale = 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION_MEDIUM))
    
    // Exit transitions
    val slideOutToLeft: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseInOutCubic)
    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION_MEDIUM))
    
    val slideOutToRight: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseInOutCubic)
    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION_MEDIUM))
    
    val slideOutToBottom: ExitTransition = slideOutVertically(
        targetOffsetY = { fullHeight -> fullHeight },
        animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseInOutCubic)
    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION_MEDIUM))
    
    val fadeOut: ExitTransition = androidx.compose.animation.fadeOut(
        animationSpec = tween(ANIMATION_DURATION_MEDIUM)
    )
    
    val scaleOut: ExitTransition = scaleOut(
        targetScale = 0.8f,
        animationSpec = tween(ANIMATION_DURATION_SHORT)
    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
}

// Item animations for lists and collection components
object ItemAnimations {
    val enterFadeExpand = fadeIn(
        animationSpec = tween(ANIMATION_DURATION_MEDIUM)
    ) + expandVertically(
        animationSpec = tween(ANIMATION_DURATION_MEDIUM)
    )
    
    val enterFadeExpandScale = fadeIn(
        animationSpec = tween(ANIMATION_DURATION_MEDIUM)
    ) + expandVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    ) + scaleIn(
        initialScale = 0.9f,
        animationSpec = tween(ANIMATION_DURATION_MEDIUM)
    )
    
    val enterSlideInHorizontally = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseInOutCubic)
    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION_MEDIUM))
    
    val exitFadeShrink = fadeOut(
        animationSpec = tween(ANIMATION_DURATION_SHORT)
    ) + shrinkVertically(
        animationSpec = tween(ANIMATION_DURATION_SHORT)
    )
    
    val exitFadeShrinkScale = fadeOut(
        animationSpec = tween(ANIMATION_DURATION_SHORT)
    ) + shrinkVertically(
        animationSpec = tween(ANIMATION_DURATION_SHORT)
    ) + scaleOut(
        targetScale = 0.9f,
        animationSpec = tween(ANIMATION_DURATION_SHORT)
    )
    
    val exitSlideOutHorizontally = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(ANIMATION_DURATION_SHORT, easing = EaseInOutCubic)
    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
} 